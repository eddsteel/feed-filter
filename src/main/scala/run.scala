package com.eddsteel.feedfilter

import net._
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Await}
import scala.language.postfixOps

object Main {
  private implicit val ec = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    Await.result({
      Future.traverse(FeedFilters.allFeeds) { feed =>
        val res = Proxying.proxy(feed)(ec)
        res.foreach { s: String => println(s) }
        res
      }
    }, 10 seconds)
  }
}
