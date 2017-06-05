package com.eddsteel.feedfilter.model

trait FeedItemExtractor[+A] {
  def apply(item: FeedItem): A
}

object FeedItemExtractor {
  def apply[A](f: FeedItem => A): FeedItemExtractor[A] =
    new FeedItemExtractor[A] {
      def apply(item: FeedItem): A =
        f(item)
    }

  def Title: FeedItemExtractor[String] =
    FeedItemExtractor(_.title.toLowerCase)

  def Description: FeedItemExtractor[String] =
    FeedItemExtractor(_.description.toLowerCase)
}
