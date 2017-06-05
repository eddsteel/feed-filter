package com.eddsteel.feedfilter

import model._
import model.config._
import model.Errors.{ConfigLoadError, ConfigParseError}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

import scala.io.Source

object FeedFilters {
  type ConfigErrors = NonEmptyList[ConfigParseError]

  def allFeeds: Either[ConfigErrors, List[FeedFilter[String]]] = {
    def accumulate[L, R](in: List[ValidatedNel[L, R]]): Either[NonEmptyList[L], List[R]] = {
      val (lefts, rights) = in.foldRight((List.empty[L], List.empty[R])) {
        case (Invalid(es), (ls, rs)) => (es.toList ++ ls, rs)
        case (Valid(r), (ls, rs)) => (ls, r :: rs)
      }

      lefts match {
        case (l :: ls) =>
          Left[NonEmptyList[L], List[R]](NonEmptyList(l, ls))
        case _ =>
          Right[NonEmptyList[L], List[R]](rights)
      }
    }

    def wrap[R](either: Either[ConfigParseError, R]): Either[ConfigErrors, R] =
      either.leftMap(NonEmptyList(_, Nil))

    val sourceE: Either[ConfigErrors, String] =
      wrap(
        Either
          .catchOnly[RuntimeException](Source.fromResource("feeds.yaml"))
          .map(_.mkString)
          .leftMap[ConfigParseError](e => ConfigLoadError(e)))

    for {
      source <- sourceE
      config <- wrap(YamlFeedConfig.parse(source.mkString))
      validatedConfig <- accumulate(config.map(_.toFeedConfig))
      filters = validatedConfig.map(_.toFeedFilter)
    } yield filters
  }
}
