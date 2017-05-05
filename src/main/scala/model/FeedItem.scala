package com.eddsteel.feedfilter.model

import java.net.URI
import scala.util.{ Try, Success, Failure }

final case class FeedItem(
  id: String,
  title: String,
  href: URI,
  description: String)

object FeedItem {
  import scala.xml._

  def fromXML(s: String): FeedItem = {
    val safe = for {
      xml <- Try(XML.loadString(s"<root>$s</root>"))
      id <- Try((xml \ "guid").text)
      title <- Try((xml \ "title").text)
      link <- Try((xml \ "link").text)
      href <- Try(new URI(link))
      description <- Try((xml \ "description").text)
    } yield FeedItem(id, title, href, description)

    safe.recoverWith {
      case e =>
        println(s"got $e when trying to parse $s")
        Failure(e)
    } match {
      case Success(fi) => fi
      case Failure(e) => ???
    }
  }
}
