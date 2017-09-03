package com.eddsteel.feedfilter
package net

import model._
import model.Errors._

import cats.instances.string._
import org.http4s._
import org.http4s.dsl._
import org.log4s
import fs2.{Strategy, Task}

import scala.concurrent.ExecutionContext

object Service {
  private val logger = log4s.getLogger

  def create(feeds: Map[String, FeedFilter[String]])(
    implicit ec: ExecutionContext,
    s: Strategy): HttpService =
    HttpService {

      case GET -> Root / "feed" / "feed-filter.service" =>
        StaticFile.fromResource("/feed-filter.service").value.flatMap { maybeOk =>
          maybeOk.map(Task.now).getOrElse(NotFound())
        }

      case request @ GET -> Root / "feed" / name =>
        feeds.get(name) match {
          case Some(feed) =>
            val conditionalGetHeaders =
              ConditionalGetHeader.collectFromRequest(request.headers).map(_.toHttpHeader)

            def proxied = Proxying.proxy(conditionalGetHeaders, feed).value

            Task.fromFuture(proxied).flatMap {
              case Right(Feed(_, result)) =>
                logger.info("OK")
                val rawHeaders = conditionalGetHeaders.map { case (k, v) => Header(k, v) }
                Ok(result).putHeaders(rawHeaders: _*)

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
