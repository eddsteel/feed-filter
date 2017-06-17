package com.eddsteel.feedfilter
package net

import com.eddsteel.feedfilter.model.ConditionalGetHeader
import xml.XmlFilter
import model.{FeedFilter, SuccessResponse, Feed, Unchanged}
import model.Errors._

import cats.Show
import cats.data._
import cats.implicits._
import scalaj.http._

import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.util.Try
import java.net.URI

object Proxying {
  private val logger = org.log4s.getLogger
  type FetchResult[A] = EitherT[Future, FetchError, A]

  def proxy[A: Show](headers: List[(String, String)], feedFilter: FeedFilter[A])(
    implicit ec: ExecutionContext): EitherT[Future, ProxyError, SuccessResponse] =
    fetch(feedFilter.src, headers, 0).flatMap {
      case Feed(headers, feed) =>
        EitherT(Future.successful({
          XmlFilter(ItemFilter.filterItem(_, feedFilter))(feed).filter.map {
            Feed(headers, _)
          }
        }))
      case unchanged @ Unchanged =>
        EitherT.pure[Future, ProxyError, SuccessResponse](unchanged)
    }

  /** A successful `None` indicates a conditional get with an "unchanged" response. */
  def fetch(u: URI, headers: List[(String, String)], chainLength: Int)(
    implicit ec: ExecutionContext): FetchResult[SuccessResponse] = {
    logger.info(s"FETCH $u")
    def left(e: FetchError): EitherT[Future, FetchError, SuccessResponse] =
      EitherT.left(Future.successful(e))

    if (chainLength > 3)
      left(TooManyRedirects(u))
    else {
      val request = headers.foldLeft(Http(u.toString)) {
        case (req, header @ (key, value)) =>
          logger.debug(s"Adding conditional header: $header")
          req.header(key, value)
      }

      val resp: FetchResult[HttpResponse[String]] = EitherT.right(Future {
        blocking {
          request.asString
        }
      })

      val result: FetchResult[SuccessResponse] = resp.flatMap { resp =>
        val body = resp.body

        resp.code match {
          case 200 =>
            val headers = ConditionalGetHeader.collectFromResponse(resp.headers)
            logger.debug(s"Including conditional headers: $headers")
            EitherT.pure[Future, FetchError, SuccessResponse](Feed(headers, body))

          case 304 =>
            EitherT.pure[Future, FetchError, SuccessResponse](Unchanged)

          case 301 =>
            val nextLocation =
              for {
                location <- resp
                  .header("Location")
                  .toRight(ServerFailedError("301 with no Location"))
                validLocation <- Try(new URI(location)).toOption
                  .toRight(ServerFailedError("301 with invalid Location"))
              } yield validLocation

            nextLocation.fold(left, fetch(_, headers, chainLength + 1))
          case 404 => left(NotFoundError(u))
          case 500 => left(ServerFailedError(body))
          case c => left(UnhandledHttpCodeError(c))
        }
      }

      EitherT(result.value.recover {
        case e => Left[FetchError, SuccessResponse](UnhandledFetchError(e))
      })
    }
  }
}
