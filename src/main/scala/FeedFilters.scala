package com.eddsteel.feedfilter

import model._
import java.net.URI

object FeedFilters {
  // TODO: load from config
  def allFeeds: List[FeedFilter[String]] = List(FeedFilter(
      name = "chapo",
      src = new URI("http://feeds.soundcloud.com/users/soundcloud:users:211911700/sounds.rss"),
      extract = FeedItemExtractor.title,
      rule = FilterNotRule(ContainsMatcher("""\bTeaser\b""".r).test)))
}
