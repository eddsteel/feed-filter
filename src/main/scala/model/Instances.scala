package com.eddsteel.feedfilter
package model

import cats.Show

import java.util.Date

object Instances {

  implicit val showDates: Show[HttpDate] = Show { h =>
    Show.fromToString[Date].show(h.date)
  }
}
