package com.eddsteel.feedfilter

import net.Service
import model.FeedFilter

import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import org.log4s
import fs2.{Stream, Task}

object Main extends StreamApp {

  private val logger = log4s.getLogger

  private val feeds: Map[String, FeedFilter[String]] = FeedFilters.allFeeds match {
    case Right(feeds) =>
      feeds.map { ff =>
        ff.name -> ff
      }.toMap
    case Left(errors) =>
      logger.error(s"BAIL $errors")
      sys.error(s"BAIL $errors")
  }

  private val service: HttpService =
    Service.create(feeds)

  def stream(args: List[String]): Stream[Task, Nothing] =
    BlazeBuilder.bindHttp(8080, "0.0.0.0").mountService(service, "/").serve
}
