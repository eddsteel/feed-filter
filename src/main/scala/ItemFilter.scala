package com.eddsteel.feedfilter

import model._

import cats.Show
import cats.implicits._

/** This is really all there is to it.
  */
object ItemFilter {
  private val logger = org.log4s.getLogger

  private def summary[B: Show](b: B): String = {
    val ellipsis = if (b.show.length > 70) "..." else ""
    b.show.take(70).takeWhile(_ =!= '\n') + ellipsis
  }

  def filterItem[A: Show](item: FeedItem, filter: FeedFilter[A]): Boolean = {
    val extractedField = filter.extract(item)
    val result = filter.rule.include(extractedField)
    if (!result) logger.info(s"SKIP ${summary(extractedField)}")

    result
  }
}
