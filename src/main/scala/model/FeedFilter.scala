package com.eddsteel.feedfilter.model

import Errors.FeedItemParseError
import cats.implicits._
import cats.Show

import java.net.URI

/** Represents a filter definition.
  */
final case class FeedFilter[A: Show](
  name: String,
  src: URI,
  extract: FeedItemExtractor[A],
  rule: FeedFilterRule[A]) {

  require(!name.contains(' '))

  private def summary[B: Show](b: B): String = {
    val ellipsis = if (b.show.length > 70) "..." else ""
    b.show.take(70).takeWhile(_ =!= '\n') + ellipsis
  }

  def itemFilter(source: String): Either[FeedItemParseError, Boolean] =
    for {
      item <- FeedItem.fromXML(source)
      extracted = extract(item)
      result = rule.include(extracted)
      _ = if (!result) println(s"SKIP ${summary(extracted)}")
    } yield result
}
