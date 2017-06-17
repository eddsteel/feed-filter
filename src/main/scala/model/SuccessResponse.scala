package com.eddsteel.feedfilter
package model

/** A success response may either be a filtered feed or a 304 Not Modified */
sealed trait SuccessResponse extends Product with Serializable
final case class Feed(headers: List[ConditionalGetHeader], feed: String) extends SuccessResponse
final case object Unchanged extends SuccessResponse
