package com.eddsteel.feedfilter
package model

import Instances._

import cats.Show
import cats.implicits._

sealed trait ConditionalGetHeader extends Product with Serializable {
  def toHttpHeader: (String, String)
}

object ConditionalGetHeader {
  def collectFromRequest(headers: Map[String, String]): List[ConditionalGetHeader] =
    headers.toList.collect {
      case ("If-Match", etag: String) => IfMatch(etag): ConditionalGetHeader
      case ("If-None-Match", etag: String) => IfMatch(etag)
      case ("If-Modified-Since", HttpDateish(d)) => IfModifiedSince(d)
      case ("If-Unmodified-Since", HttpDateish(d)) => IfUnmodifiedSince(d)
      case ("If-Range", anything: String) => IfRange(anything)
    }

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
