package com.eddsteel.feedfilter.model

import scala.util.matching.Regex

final case class ContainsMatcher(regex: Regex) {
  def test(string: String): Boolean =
    regex.findFirstIn(string).nonEmpty
}
