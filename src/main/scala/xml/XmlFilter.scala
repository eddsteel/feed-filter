package com.eddsteel.feedfilter
package xml

import model.Errors._
import cats.implicits._

import scala.io.Source
import scala.xml._
import scala.xml.pull._

/** XML filtering is gross. Hidden here.
  */
class XmlFilter private (source: String, itemFilter: String => Either[XmlFilteringError, Boolean]) {
  import XmlFilter._

  private def tag(pre: String, name: String) =
    if (Option(pre).exists(_.nonEmpty)) s"$pre:$name"
    else name

  def filter: Either[XmlFilteringError, String] = {
    val eventReader = new XMLEventReader(Source.fromString(source))

    val out: IntermediateParse = eventReader.foldLeft(XmlPartialOutput.empty) {
      case (l @ Left(_), _) => l

      case (r, EvProcInstr("xml", text)) =>
        r.map(_.append(s"<?xml$text?>"))

      case (r, EvElemStart(p, e @ "rss", as, ns)) =>
        r.map(_.append(s"<${tag(p, e)}${ns.toString}${as.toString}>"))

      case (r, EvElemStart(_, e @ "item", as, _))
          if (r.exists(_.itemInProgress.isDefined) || as.nonEmpty) =>
        Left[XmlFilteringError, XmlPartialOutput](MalformedXML("item"))

      case (r, EvElemStart(_, e @ "item", _, _)) =>
        r.map(_.startItem)

      case (r, EvElemEnd(_, e @ "item")) =>
        r.flatMap(_.endItem(itemFilter))

      case (r, EvElemStart(p, e, as, _)) =>
        r.map(_.append(s"<${tag(p, e)}${as.toString}>"))

      case (r, EvElemEnd(p, e)) =>
        r.map(_.append(s"</${tag(p, e)}>"))

      case (r, EvText(t)) =>
        r.map(_.append(t))

      case (r, EvEntityRef(ref)) =>
        r.map(_.append(s"&$ref;"))

      case (_, ev) =>
        Left[XmlFilteringError, XmlPartialOutput](UnhandledStreamEvent(ev))
    }

    out.map(_.complete)
  }
}

object XmlFilter {
  type IntermediateParse = Either[XmlFilteringError, XmlPartialOutput]

  def apply(filter: String => Either[XmlFilteringError, Boolean])(source: String): XmlFilter =
    new XmlFilter(source, filter)
}

final case class XmlPartialOutput(
  output: StringBuilder,
  itemInProgress: Option[StringBuilder],
  drop: Boolean) {

  def complete: String = output.toString

  def on: XmlPartialOutput = copy(drop = false)

  def off: XmlPartialOutput = copy(drop = true)

  def append(s: String): XmlPartialOutput = itemInProgress match {
    case Some(buf) =>
      copy(itemInProgress = Some(buf.append(s)))
    case None =>
      copy(output = output.append(s))
  }

  def startItem: XmlPartialOutput =
    copy(itemInProgress = Some(new StringBuilder))

  def endItem(filter: String => Either[XmlFilteringError, Boolean])
    : Either[XmlFilteringError, XmlPartialOutput] = {
    val item = itemInProgress.map(_.toString).getOrElse("")
    filter(item).map { filtered =>
      val addition =
        if (filtered) s"<item>$item</item>" // <foo>{bar}</foo> escapes
        else ""

      copy(itemInProgress = None, output = output.append(addition))
    }
  }
}

object XmlPartialOutput {
  def empty: XmlFilter.IntermediateParse =
    Right[XmlFilteringError, XmlPartialOutput](
      XmlPartialOutput(
        new StringBuilder("""<?xml version="1.0" encoding="UTF-8"?>"""),
        None,
        false))
}
