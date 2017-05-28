package com.eddsteel.feedfilter
package net

import model.Errors._

import org.scalatra._

import scala.concurrent.{ExecutionContext, Future}

object Servlet extends ScalatraServlet with FutureSupport {
  override val executor: ExecutionContext = ExecutionContext.global
  implicit val ec: ExecutionContext = executor

  private val feeds = FeedFilters.allFeeds.map { ff =>
    ff.name -> ff
  }.toMap

  get("/feed/:name") {
    feeds.get(params("name")) match {
      case Some(feed) =>
        Proxying.proxy(feed).value.map {
          case Right(result) =>
            Ok(result)

          case Left(NotFoundError(u)) =>
            println(s"$u not found")
            NotFound()

          case Left(TooManyRedirects(_)) =>
            ActionResult(ResponseStatus(310, "Too many redirects"), "", Map.empty)

          case Left(e) =>
            println(s"failed with $e")
            InternalServerError
        }
      case None =>
        Future.successful(NotFound("File Not Found"))
    }
  }
}
