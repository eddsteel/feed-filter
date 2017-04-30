package com.eddsteel.feedfilter

import model._
import net._

import java.io.ByteArrayInputStream
import java.net.URI
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Await, blocking }
import scala.io.Source
import scala.language.postfixOps

object Main {
  private implicit val ec = ExecutionContext.global

  def main(args: Array[String]): Unit =
    Await.result({
      Future.traverse(FeedFilters.allFeeds) { feed =>
        Proxying.proxy(feed).map(println)
      }
    }, 10 seconds)
}
