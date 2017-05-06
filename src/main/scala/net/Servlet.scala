package com.eddsteel.feedfilter
package net

import cats._
import cats.data._
import cats.implicits._
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
      case Some(feed) => Proxying.proxy(feed).value.map {
        case Right(result) => Ok(result)
        case Left(l) =>
          println(l)
          InternalServerError("nuh")
      }
      case None => Future.successful(NotFound("File Not Found"))
    }
  }
}
