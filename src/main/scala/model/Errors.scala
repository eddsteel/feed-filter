package com.eddsteel.feedfilter.model

import cats.data.NonEmptyList
import scala.xml.pull.XMLEvent
import java.net.URI

object Errors {

  // https://github.com/wartremover/wartremover/issues/162
  sealed trait ProxyError extends Product with Serializable

  sealed trait FetchError extends ProxyError
  final case class NotFoundError(url: URI) extends FetchError
  final case class ServerFailedError(reason: String) extends FetchError
  final case class TooManyRedirects(uri: URI) extends FetchError
  final case class UnhandledHttpCodeError(code: Int) extends FetchError
  final case class UnhandledFetchError(cause: Throwable) extends FetchError

  sealed trait XmlFilteringError extends ProxyError
  final case class UnhandledStreamEvent(node: XMLEvent) extends XmlFilteringError
  final case class MalformedXML(tag: String) extends XmlFilteringError

  sealed trait FeedItemParseError extends XmlFilteringError
  final case class SaxProblem(t: Throwable) extends FeedItemParseError
  final case class FeedItemMarshalError(problems: NonEmptyList[XmlMarshalProblem])
      extends FeedItemParseError

  sealed trait XmlMarshalProblem
  final case class AttributeMarshalProblem(key: String, value: Option[String])
      extends XmlMarshalProblem

  sealed trait ConfigParseError extends Product with Serializable
  final case class UnknownFeedItemExtractor(given: String) extends ConfigParseError
  final case class MissingFeedFilterRuleField(required: String) extends ConfigParseError
  final case class UnknownFeedFilterRule(given: Map[String, String]) extends ConfigParseError
  final case class ConfigLoadError(t: Throwable) extends ConfigParseError
  final case class YamlParseError(t: Throwable) extends ConfigParseError
}
