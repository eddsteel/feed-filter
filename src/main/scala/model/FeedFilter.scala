package com.eddsteel.feedfilter.model

import Errors.FeedItemParseError

import java.net.URI

/** Represents a filter definition.
  */
final case class FeedFilter[A](
  name: String,
  src: URI,
  extract: FeedItemExtractor[A],
  rule: FeedFilterRule[A]) {

  require(!name.contains(' '))

  def itemFilter(source: String): Either[FeedItemParseError, Boolean] =

  for {
    item <- FeedItem.fromXML(source)
    extracted = extract(item)
    result = rule.include(extracted)
    _ = if (!result) println(s"SKIP $extracted")
  } yield result
}
