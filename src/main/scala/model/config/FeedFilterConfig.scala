package com.eddsteel.feedfilter
package model.config

import com.eddsteel.feedfilter.model._
import model.FeedFilter
import cats.implicits._

import java.net.URI
import scala.util.matching.Regex
import scala.util.Try

final case class FeedConfig(
  name: String,
  src: URI,
  extract: ExtractorChoice,
  rule: RuleConfig) {

  def toFeedFilter: FeedFilter[String] = // let's punt on abstracting over this type
    FeedFilter(name, src, extract.toExtractor, rule.toRule)
}

sealed trait ExtractorChoice { def toExtractor: FeedItemExtractor[String]}
final object ExtractorChoice {
  @SuppressWarnings(Array("org.wartremover.warts.Product", "org.wartremover.warts.Serializable"))
  private val choices: Map[String, ExtractorChoice] =
    Map(
      "title" -> TitleExtractorChoice,
      "description" -> DescriptionExtractorChoice)

  def fromString(name: String): Option[ExtractorChoice] =
    choices.get(name)
}

final case object TitleExtractorChoice extends ExtractorChoice {
  def toExtractor: FeedItemExtractor[String] = FeedItemExtractor.Title
}

final case object DescriptionExtractorChoice extends ExtractorChoice {
  def toExtractor: FeedItemExtractor[String] = FeedItemExtractor.Description
}

sealed trait RuleConfig { def toRule: FeedFilterRule[String] }
final object RuleConfig {
  private val fields: Map[String, Seq[String]] =
    Map("filter-not" -> Seq("matcher"))

  def fromFields(`type`: String, config: Map[String, String]): Option[RuleConfig] = {
    val fieldValues = for {
      fs <- fields.get(`type`)
      fvs = fs.zip(fs.map(config.get))
    } yield (`type`, fvs)

    fieldValues.flatMap {
      case ("filter-not", Seq(("matcher", Some(m)))) => for {
        pattern <- Try(m.r).toOption
        config <- Some(FilterNotMatchRuleConfig(pattern))
      } yield config
    }
  }
}

final case class FilterNotMatchRuleConfig(`match`: Regex) extends RuleConfig {
  def toRule = FilterNotRule(ContainsMatcher(`match`).test)
}
