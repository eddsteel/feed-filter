package com.eddsteel.feedfilter
package net

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
      case Some(feed) => Proxying.proxy(feed).map(Ok(_))
      case None => Future.successful(NotFound("File Not Found"))
    }
  }
}
