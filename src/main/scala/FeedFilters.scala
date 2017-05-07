package com.eddsteel.feedfilter

import model._
import cats.implicits._

import java.net.URI

object FeedFilters {
  // TODO: load from config
  def allFeeds: List[FeedFilter[String]] =
    List(
      FeedFilter(
        name = "chapo",
        src = new URI("http://feeds.soundcloud.com/users/soundcloud:users:211911700/sounds.rss"),
        extract = FeedItemExtractor.title,
        rule = FilterNotRule(ContainsMatcher("""\bteaser\b""".r).test)
      ),
      FeedFilter(
        name = "wittertainment",
        src = new URI("http://www.bbc.co.uk/programmes/b00lvdrj/episodes/downloads.rss"),
        extract = FeedItemExtractor.description,
        rule = FilterNotRule(
          ContainsMatcher("""\bsit(s|ting)? in for (mark|simon|kermode|mayo)\b| presents """.r).test)
      ))
}
