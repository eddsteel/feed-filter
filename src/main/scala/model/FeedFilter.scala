package com.eddsteel.feedfilter
package model

import java.net.URI

/** Represents a filter definition.
  */
final case class FeedFilter[A](
  name: String,
  src: URI,
  extract: FeedItemExtractor[A],
  rule: FeedFilterRule[A]) {
  require(!name.contains(' '))
}
