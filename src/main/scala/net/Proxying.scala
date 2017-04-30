package com.eddsteel.feedfilter
package net

import model._
import xml._
import scalaj.http._

import scala.concurrent.{ExecutionContext, Future, blocking}
import java.net.URI

object Proxying {
  def proxy(feedFilter: FeedFilter[_])(implicit ec: ExecutionContext): Future[String] = {
    fetch(feedFilter.src).map { s =>
      XmlFilter(feedFilter.itemFilter)(s).filter
    }
  }

  def fetch(u: URI)(implicit ec: ExecutionContext): Future[String] = Future(blocking(
    Http(u.toString).asString.body)) // FIX Error handling
}
