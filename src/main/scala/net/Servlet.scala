package com.eddsteel.feedfilter
package net

import model.FeedFilter
import model.Errors._

import cats.implicits._
import org.scalatra._

import scala.concurrent.{ExecutionContext, Future}

object Servlet extends ScalatraServlet with FutureSupport {
  override val executor: ExecutionContext = ExecutionContext.global
  implicit val ec: ExecutionContext = executor
  private val logger = org.log4s.getLogger

  private val feeds = FeedFilters.allFeeds.map { ff =>
    ff.name -> ff
  }.toMap

  get("/feed/:name") {
    val feedName = params("name")
    feeds.get(feedName) match {
      case Some(feed: FeedFilter[String]) =>
        Proxying.proxy(feed).value.map {
          case Right(result) =>
            Ok(result)

          case Left(NotFoundError(u)) =>
            logger.error(s"$u not found")
            NotFound(s"$u not found")

          case Left(TooManyRedirects(u)) =>
            logger.error(s"$u caused too many redirects")
            ActionResult(ResponseStatus(310, "Too many redirects"), "", Map.empty)

          case Left(e) =>
            logger.error(s"Failed with $e")
            InternalServerError
        }
      case None =>
        logger.error(s"Can't find feed ($feedName)")
        Future.successful(NotFound(feedName))
    }
  }
}
