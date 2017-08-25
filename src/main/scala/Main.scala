package com.eddsteel.feedfilter

import net.Service
import model.FeedFilter

import org.http4s.server.blaze._
import org.http4s.util.ProcessApp
import org.log4s
import scalaz.concurrent.Task
import scalaz.stream.Process

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends ProcessApp {

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

  private val service = Service.create(feeds)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  override def main(args: List[String]): Process[Task, Nothing] =
    BlazeBuilder.bindHttp(8080, "localhost").mountService(service, "/").serve
}
