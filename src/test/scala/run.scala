package com.eddsteel.feedfilter

import net._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object Main {
  private implicit val ec = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val feeds = FeedFilter.allFeeds match {
      case Left(errors) => sys.error(errors)
      case Right(fs) => fs
    }

    val _ = Await.result({
      Future.traverse(feeds) { feed =>
        val res = Proxying.proxy(feed)(ec).value
        res
      }
    }, 10 seconds)
    ()
  }
}
