package com.eddsteel.feedfilter
package model

import Instances._

import cats.Show
import cats.implicits._
import org.http4s.Headers
import org.http4s.Header
import org.http4s.headers._

sealed trait ConditionalGetHeader extends Product with Serializable {
  def toHttpHeader: (String, String)
}

// TODO make use of http4s headers with CaseInsensitiveString
object ConditionalGetHeader {

  private val logger = org.log4s.getLogger

  def collectFromRequest(headers: Headers): List[ConditionalGetHeader] =
    headers.toList.collect {
      case Header(`If-Match`.name, etag) => Some(IfMatch(etag)): Option[ConditionalGetHeader]
      case Header(`If-None-Match`.name, etag: String) => Some(IfMatch(etag))
      case Header(`If-Modified-Since`.name, HttpDateish(d)) => Some(IfModifiedSince(d))
      case Header(`If-Unmodified-Since`.name, HttpDateish(d)) => Some(IfUnmodifiedSince(d))
      case Header(`If-Range`.name, anything: String) => Some(IfRange(anything))
      case x => logger.debug(s"!!! Skipping $x"); None
    }.map(_.toList).flatten

  def collectFromResponse(headers: Map[String, Seq[String]]): List[ConditionalGetHeader] =
    headers.toList.collect {
      case ("ETag", etags) =>
        etags.map(ETag.apply): Seq[ConditionalGetHeader]

      case ("etag", etags) =>
        etags.map(ETag.apply) // oh hai HTTP2 servers

      case ("Last-Modified", dates) =>
        dates.collect {
          case HttpDateish(d) => LastModified(d)
        }
      case ("last-modified", dates) =>
        dates.collect {
          case HttpDateish(d) => LastModified(d)
        }
    }.flatten
}

sealed abstract class KVCGHeader[V: Show](key: String, value: V) extends ConditionalGetHeader {
  def toHttpHeader: (String, String) = key -> value.show
}

// requests
//
final case class IfMatch(etag: String) extends KVCGHeader("If-Match", etag)
final case class IfNoneMatch(etag: String) extends KVCGHeader("If-None-Match", etag)
final case class IfModifiedSince(date: HttpDate) extends KVCGHeader("If-Modified-Since", date)
final case class IfUnmodifiedSince(date: HttpDate) extends KVCGHeader("If-Unmodified-Since", date)
final case class IfRange(value: String) extends KVCGHeader("If-Range", value)

// responses
//
final case class ETag(etag: String) extends KVCGHeader("ETag", etag)
final case class LastModified(date: HttpDate) extends KVCGHeader("Last-Modified", date)

object HttpDateish {
  def unapply(s: String): Option[HttpDate] = HttpDate.parse(s)
}
