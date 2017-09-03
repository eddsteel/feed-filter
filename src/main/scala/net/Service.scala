package com.eddsteel.feedfilter
package net

import model._
import model.Errors._

import cats.instances.string._
import org.http4s._
import org.http4s.dsl._
import org.log4s
import fs2.Task

object Service {
  private val logger = log4s.getLogger

  def create(feeds: Map[String, FeedFilter[String]]): HttpService =
    HttpService {

      case GET -> Root / "feed-filter.service" =>
        StaticFile.fromResource("/feed-filter.service").value.flatMap { maybeOk =>
          maybeOk.map(Task.now).getOrElse(NotFound())
        }

      case request @ GET -> Root / "feed" / name =>
        feeds.get(name) match {
          case Some(feed) =>
            val conditionalGetHeaders =
              ConditionalGetHeader.filterFromRequest(request.headers)

            def proxied = Proxying.proxy(conditionalGetHeaders, feed)

            proxied.value.flatMap {
              case Right(Feed(headers, result)) =>
                logger.info("OK")
                Ok(result).putHeaders(headers.toSeq: _*)

              case Right(Unchanged) =>
                logger.info("NM")
                NotModified()

              case Left(NotFoundError(u)) =>
                logger.error(s"KO $u not found")
                NotFound(s"$u not found")

              case Left(TooManyRedirects(u)) =>
                logger.error(s"KO $u caused too many redirects")
                Task.now(Response(Status(310)("Too many redirects", false)))

              case Left(e) =>
                logger.error(s"KO Failed with $e")
                InternalServerError()
            }

          case None =>
            logger.error(s"KO Can't find feed ($name)")
            NotFound(name)
        }
    }
}
