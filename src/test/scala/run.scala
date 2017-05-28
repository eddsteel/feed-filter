package com.eddsteel.feedfilter

import net._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object Main {
  private implicit val ec = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val _ = Await.result({
      Future.traverse(FeedFilters.allFeeds) { feed =>
        val res = Proxying.proxy(feed)(ec).value
//        res.foreach(println)
        res
      }
    }, 10 seconds)
    ()
  }
}
