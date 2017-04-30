package com.eddsteel.feedfilter.xml

import scala.io.Source
import scala.xml._
import scala.xml.pull._


/** XML filtering is gross. Hidden here.
  */
class XmlFilter private(source: String, itemFilter: String => Boolean) {

  private def tag(pre: String, name: String) =
    if (Option(pre).exists(_.nonEmpty)) s"$pre:$name"
    else name

  def filter: String = {
    val eventReader = new XMLEventReader(Source.fromString(source))

    val out = eventReader.foldLeft(XmlPartialOutput.empty) {
      case (x, EvProcInstr("xml", text)) =>
        x.append(s"<?xml$text?>")

      case (x, EvElemStart(p, e @ "rss", as, ns)) =>
        x.append(s"<${tag(p, e)}${ns.toString}${as.toString}>")

      case (x, EvElemStart(p, e @ "item", as, ns)) if (x.itemInProgress.isDefined || as.nonEmpty) =>
        throw new RuntimeException("Yeearrgh")

      case (x, EvElemStart(p, e @ "item", as, ns)) =>
        x.startItem

      case (x, EvElemEnd(p, e @ "item")) =>
        x.endItem(itemFilter)

      case (x, EvElemStart(p, e, as, ns)) =>
        x.append(s"<${tag(p, e)}${as.toString}>")

      case (x, EvElemEnd(p, e)) =>
        x.append(s"</${tag(p, e)}>")

      case (x, EvText(t)) =>
        x.append(t)

      case (x, EvEntityRef(r)) =>
        x.append(s"&$r;")

      case (x, node) =>
        println(node)
        x
/*
      case (x, EvComment(c)) =>
        x.append(Comment(c)).on


      case (x, a: Atom[_]) => ???
      case (x, Comment(_)) => ???
      case (x, d: Document) => ???
      case (x, EntityRef(_)) => ???
      case (x, p: PCData) => ???
      case (x, ProcInstr(_, _)) => ???
      case (x, t: Text) => ???
      case (x, u: Unparsed) => ??? */
    }

    out.complete
  }
}

object XmlFilter {
  def apply(filter: String => Boolean)(source: String): XmlFilter =
    new XmlFilter(source, filter)
}

case class XmlPartialOutput(
  output: StringBuilder,
  itemInProgress: Option[StringBuilder],
  drop: Boolean) {
  def complete = output.toString
  def on = copy(drop = false)
  def off = copy(drop = true)
  def append(s: String) = itemInProgress match {
    case Some(buf) =>
      buf.append(s)
      copy(itemInProgress = Some(buf))
    case None =>
      copy(output = output.append(s))
  }

  def startItem = copy(itemInProgress = Some(new StringBuilder))
  def endItem(filter: String => Boolean) = {
    val item = itemInProgress.map(_.toString).getOrElse("")
    val addition =
      if (filter(item)) s"<item>$item</item>" // <foo>{bar}</foo> escapes
      else ""

    copy(itemInProgress = None, output = output.append(addition))
  }
}


object XmlPartialOutput {
  def empty: XmlPartialOutput =
    XmlPartialOutput(
      new StringBuilder("""<?xml version="1.0" encoding="UTF-8"?>"""), None, false)
}
