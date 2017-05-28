package com.eddsteel.feedfilter.model

import Errors._
import cats.implicits._
import cats.data.{Validated, ValidatedNel}
import java.net.URI
import scala.util.Try

final case class FeedItem(id: String, title: String, href: URI, description: String)

object FeedItem {
  import scala.xml._

  type Parsed = Either[FeedItemParseError, FeedItem]

  private def handleSax[A](unsafeCall: => A): Validated[FeedItemParseError, A] =
    Validated.fromTry(Try(unsafeCall)).leftMap(SaxProblem.apply)

  def handleAttr[A](unsafeCall: => A)(key: String): ValidatedNel[XmlMarshalProblem, A] =
    Validated
      .fromTry(Try(unsafeCall))
      .leftMap(_ => AttributeMarshalProblem(key, None))
      .toValidatedNel[XmlMarshalProblem, A]


  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  def fromXML(s: String): Parsed = {
    val xml = handleSax(XML.loadString(s"<root>$s</root>")).toEither
    xml.flatMap { doc =>
      val validated = (handleAttr[String]((doc \ "guid").text)("guid") |@|
        handleAttr[String]((doc \ "title").text)("title") |@|
        handleAttr[String]((doc \ "link").text)("link").map(new URI(_)) |@|
        handleAttr[String]((doc \ "description").text)("description")).map(FeedItem.apply _)

      val end: Parsed = validated.leftMap(FeedItemMarshalError.apply).toEither
      end
    }
  }
}
