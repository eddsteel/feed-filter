package com.eddsteel.feedfilter
package net

import model.{ConditionalGetHeader, FeedFilter}
import model.Errors._

import cats.implicits._
import org.scalatra._

import scala.concurrent.{ExecutionContext, Future}

object Servlet extends ScalatraServlet with FutureSupport {
  override val executor: ExecutionContext = ExecutionContext.global
  implicit val ec: ExecutionContext = executor
  private val logger = org.log4s.getLogger

  private val feeds: Map[String, FeedFilter[String]] = FeedFilters.allFeeds match {
    case Right(feeds) =>
      feeds.map { ff =>
        ff.name -> ff
      }.toMap
    case Left(errors) =>
      logger.error(s"BAIL $errors")
      sys.error(s"BAIL $errors")
  }

  get("/feed/:name") {
    val feedName = params("name")
    val conditionalGetHeaders =
      ConditionalGetHeader.collectFromRequest(request.headers).map(_.toHttpHeader)

    feeds.get(feedName) match {
      case Some(feed: FeedFilter[String]) =>
        Proxying.proxy(conditionalGetHeaders, feed).value.map {
          case Right(Feed(headers, result)) =>
            logger.info("OK")
            Ok(result, headers = headers.map(_.toHttpHeader).toMap)

          case Right(Unchanged) =>
            logger.info("NM")
            NotModified

          case Left(NotFoundError(u)) =>
            logger.error(s"KO $u not found")
            NotFound(s"$u not found")

          case Left(TooManyRedirects(u)) =>
            logger.error(s"KO $u caused too many redirects")
            ActionResult(ResponseStatus(310, "Too many redirects"), "", Map.empty)

          case Left(e) =>
            logger.error(s"KO Failed with $e")
            InternalServerError
        }
      case None =>
        logger.error(s"KO Can't find feed ($feedName)")
        Future.successful(NotFound(feedName))
    }
  }
}
