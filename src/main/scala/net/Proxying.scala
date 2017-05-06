package com.eddsteel.feedfilter
package net

import model._
import xml._
import model.Errors._

import cats._
import cats.data._
import cats.implicits._
import scalaj.http._

import scala.concurrent.{blocking, ExecutionContext, Future}
import java.net.URI

object Proxying {
  def proxy(
    feedFilter: FeedFilter[_])(implicit ec: ExecutionContext): EitherT[Future, ProxyError, String] =
    fetch(feedFilter.src).flatMap { s =>
      new EitherT(Future.successful(XmlFilter(feedFilter.itemFilter)(s).filter))
    }

  def fetch(u: URI)(implicit ec: ExecutionContext): EitherT[Future, FetchError, String] = {
    val resp = Future {
      blocking {
        Http(u.toString).asString
      }
    }

    new EitherT(resp.map { resp =>
      val body = resp.body

      if (resp.code === 200) Right[FetchError, String](body)
      else resp.code match {
        case 404 => Left[FetchError, String](NotFoundError(u))
        case 500 => Left[FetchError, String](ServerFailedError(body))
        case c => Left[FetchError, String](UnhandledHttpCodeError(c))
      }
    }.recover {
      case e => Left[FetchError, String](UnhandledFetchError(e))
    })
  }
}
