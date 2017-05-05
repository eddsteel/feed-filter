package com.eddsteel.feedfilter.model

sealed trait FeedFilterRule[-A] {
  def include(a: A): Boolean
}

final case class FilterNotRule[A](determinant: A => Boolean) extends FeedFilterRule[A] {
  def include(a: A): Boolean =
    !determinant(a)
}
