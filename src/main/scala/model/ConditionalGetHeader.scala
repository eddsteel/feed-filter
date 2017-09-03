package com.eddsteel.feedfilter
package model

import org.http4s._
import org.http4s.headers._

object ConditionalGetHeader {

  private val logger = org.log4s.getLogger

  def filterFromRequest(headers: Headers): Headers =
    Headers(headers.toList.filter {
      case Header(`If-Match`.name, _) => true
      case Header(`If-None-Match`.name, _: String) => true
      case Header(`If-Modified-Since`.name, HttpDateish(_)) => true
      case Header(`If-Unmodified-Since`.name, HttpDateish(_)) => true
      case Header(`If-Range`.name, _) => true
      case x => logger.debug(s"!!! Skipping $x"); false
    })

  def filterFromResponse(headers: Headers): Headers =
    Headers(headers.toList.filter {
      case Header(org.http4s.headers.ETag.name, _) => true
      case Header(`Last-Modified`.name, HttpDateish(_)) => true
      case _ => false
    })
}

object HttpDateish {
  def unapply(s: String): Option[HttpDate] = HttpDate.parse(s)
}
