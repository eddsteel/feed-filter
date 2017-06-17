package com.eddsteel.feedfilter
package model

import java.util.{Date, Locale, TimeZone}
import java.text.SimpleDateFormat
import scala.util.Try

/** In HTTP dates are in the following format:
  * `Wed, 21 Oct 2015 07:28:00 GMT`
  * and always expressed as GMT.
  */
final class HttpDate(val date: Date) extends AnyVal

object HttpDate {
  private val dateFormat: SimpleDateFormat = {
    val df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"))
    df
  }

  def parse(s: String): Option[HttpDate] =
    Try(dateFormat.parse(s)).toOption.map(new HttpDate(_))

  def format(d: HttpDate): String =
    dateFormat.format(d.date)
}
