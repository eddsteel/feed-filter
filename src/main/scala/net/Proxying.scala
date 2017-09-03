package com.eddsteel.feedfilter
package net

import model.ConditionalGetHeader
import model.{Feed, FeedFilter, SuccessResponse, Unchanged}
import model.Errors._
import xml.XmlFilter

import cats.Show
import cats.data.{EitherT}
import cats.syntax.either._
import fs2.Task
import fs2.interop.cats._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.util.CaseInsensitiveString
import org.http4s._

import java.net.{URI, URISyntaxException}

object Proxying {
  private val logger = org.log4s.getLogger
  private val httpClient = PooledHttp1Client()

  type FetchResult[A] = EitherT[Task, FetchError, A]

  def proxy[A: Show](
    headers: Headers,
    feedFilter: FeedFilter[A]): EitherT[Task, ProxyError, SuccessResponse] =
    fetch(feedFilter.src, headers, 0).flatMap {
      case Feed(headers, feed) =>
        EitherT(Task.now({
          XmlFilter(ItemFilter.filterItem(_, feedFilter))(feed).filter.map {
            Feed(headers, _)
          }
        }))
      case unchanged @ Unchanged =>
        EitherT.pure[Task, ProxyError, SuccessResponse](unchanged)
    }

  def fetch(u: URI, headers: Headers, chainLength: Int): FetchResult[SuccessResponse] = {
    logger.info(s"FETCH $u")

    def left(e: FetchError): Task[Either[FetchError, SuccessResponse]] =
      Task.now(Left[FetchError, SuccessResponse](e))

    def right(s: SuccessResponse): Task[Either[FetchError, SuccessResponse]] =
      Task.now(Right[FetchError, SuccessResponse](s))

    if (chainLength > 3)
      EitherT(left(TooManyRedirects(u)))
    else {
      val request: EitherT[Task, FetchError, Request] = EitherT.fromEither {
        Uri.fromString(u.toString).leftMap(_ => BadUriError(u.toString)).map { u =>
          logger.debug(s"Using headers: $headers")
          Request(Method.GET, u, headers = headers)
        }
      }

      request.flatMap { req =>
        EitherT(httpClient.fetch(req) { resp =>
          // our XML handler can't stream yet.
          val body: Task[String] = EntityDecoder.decodeString(resp)

          resp.status match {
            case Status(200) =>
              val headers = ConditionalGetHeader.filterFromResponse(resp.headers)
              logger.debug(s"Including conditional headers: $headers")
              body.flatMap(b => right(Feed(headers, b)))

            case Status(304) =>
              right(Unchanged)

            case Status(301) =>
              val nextLocation =
                for {
                  location <- resp.headers
                    .get(CaseInsensitiveString("Location"))
                    .toRight(ServerFailedError("301 with no Location"))
                  validLocation <- Either
                    .catchOnly[URISyntaxException](new URI(location.value))
                    .leftMap(_ => ServerFailedError("301 with invalid Location"))
                } yield validLocation

              nextLocation.fold(left, fetch(_, headers, chainLength + 1).value)
            case Status(404) => left(NotFoundError(u))
            case Status(500) => body.flatMap(b => left(ServerFailedError(b)))
            case s => left(UnhandledHttpCodeError(s.code))
          }
        })
      }
    }
  }
}
