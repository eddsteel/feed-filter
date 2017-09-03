package com.eddsteel.feedfilter

import net._

import cats.implicits._
import org.http4s._
import fs2._

import scala.concurrent.ExecutionContext

object TestMain {
  private val strategy =
    Strategy.fromExecutionContext(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val feeds = FeedFilters.allFeeds match {
      case Left(errors) => sys.error(errors.toList.mkString)
      case Right(fs) => fs
    }

    val _ =
      Task
        .parallelTraverse(feeds) { feed =>
          val res = Proxying.proxy(Headers.empty, feed).value
          res
        }(strategy)
        .map(_.mkString("\n"))
        .unsafeRun
  }
}
