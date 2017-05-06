package com.eddsteel.feedfilter.model

import Errors._
import cats._
import cats.implicits._
import cats.data.{ NonEmptyList, Validated, ValidatedNel }
import java.net.URI
import scala.util.{Failure, Success, Try}

final case class FeedItem(id: String, title: String, href: URI, description: String)

object FeedItem {
  import scala.xml._

  type Parsed = Either[FeedItemParseError, FeedItem]

  private def handleSax[A](unsafeCall: => A): Validated[FeedItemParseError, A] =
    Validated.fromTry(Try(unsafeCall)).leftMap(SaxProblem.apply)

  def handleAttr[A](unsafeCall: => A)(key: String): ValidatedNel[XmlMarshalProblem, A] =
    Validated.fromTry(Try(unsafeCall)).leftMap {
      case a => AttributeMarshalProblem(key, None)
    }.toValidatedNel[XmlMarshalProblem, A]

  def fromXML(s: String): Parsed = {
    val xml = handleSax(XML.loadString(s"<root>$s</root>")).toEither
    xml.flatMap { doc =>
      val validated = (handleAttr((doc \ "guid").text)("guid") |@|
        handleAttr((doc \ "title").text)("title") |@|
        handleAttr((doc \ "link").text)("link").map(new URI(_)) |@|
        handleAttr((doc \ "description").text)("description")).map(FeedItem.apply _)

      val end: Parsed = validated.leftMap(FeedItemMarshalError.apply).toEither
      end
    }
  }
}
