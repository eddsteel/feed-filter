package com.eddsteel.feedfilter
package net

import scalaj.http.HttpResponse
import xml.XmlFilter
import model.FeedFilter
import model.Errors._

import cats.data._
import cats.implicits._
import scalaj.http._

import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.util.Try
import java.net.URI

object Proxying {
  private val logger = org.log4s.getLogger
  type FetchResult[A] = EitherT[Future, FetchError, A]

  def proxy(feedFilter: FeedFilter[_])(
    implicit ec: ExecutionContext): EitherT[Future, ProxyError, String] =
    fetch(feedFilter.src, 0).flatMap { s =>
      EitherT(Future.successful(XmlFilter(feedFilter.itemFilter)(s).filter))
    }

  def fetch(u: URI, chainLength: Int)(implicit ec: ExecutionContext): FetchResult[String] = {
    logger.info(s"FETCH $u")
    def left(e: FetchError): EitherT[Future, FetchError, String] =
      EitherT.left(Future.successful(e))

    if (chainLength > 3)
      left(TooManyRedirects(u))
    else {
      val resp: FetchResult[HttpResponse[String]] = EitherT.right(Future {
        blocking {
          Http(u.toString).asString
        }
      })

      val result: FetchResult[String] = resp.flatMap { resp =>
        val body = resp.body

        if (resp.code === 200)
          EitherT.pure[Future, FetchError, String](body)
        else
          resp.code match {
            case 301 =>
              val nextLocation =
                for {
                  location <- resp.header("Location").toRight(
                    ServerFailedError("301 with no Location"))
                  validLocation <- Try(new URI(location)).toOption.toRight(
                    ServerFailedError("301 with invalid Location"))
                } yield validLocation

              nextLocation.fold(left, fetch(_, chainLength + 1))
            case 404 => left(NotFoundError(u))
            case 500 => left(ServerFailedError(body))
            case c => left(UnhandledHttpCodeError(c))
          }
      }

      EitherT(result.value.recover {
        case e => Left[FetchError, String](UnhandledFetchError(e))
      })
    }
  }
}
