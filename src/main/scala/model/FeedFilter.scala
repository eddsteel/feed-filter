package com.eddsteel.feedfilter.model

import java.net.URI

/** Represents a filter definition.
  */
final case class FeedFilter[A](
  name: String,
  src: URI,
  extract: FeedItemExtractor[A],
  rule: FeedFilterRule[A]) {

  require(! name.contains(' '))

  def itemFilter(source: String): Boolean = {
    val item = FeedItem.fromXML(source)
    val extracted = extract(item)
    val result = rule.include(extracted)
    if (! result) println(s"SKIP $extracted")

    result
  } // FIX error handling
}
