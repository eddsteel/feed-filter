package com.eddsteel.feedfilter.model

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
}
