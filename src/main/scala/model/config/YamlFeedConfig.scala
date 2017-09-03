package com.eddsteel.feedfilter.model.config

import com.eddsteel.feedfilter.model.Errors._
import cats.data.{Validated, ValidatedNel}
import cats.implicits._
import net.jcazevedo.moultingyaml._

import java.net.URI

final case class YamlFeedConfig(name: String, src: URI, extract: String, rule: Map[String, String]) {

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.Any",
      "org.wartremover.warts.Nothing"
    ))
  def toFeedConfig: ValidatedNel[ConfigParseError, FeedConfig] = {
    val validatedRule: Validated[ConfigParseError, RuleConfig] = (for {
      ruleType <- rule.get("type").toRight(MissingFeedFilterRuleField("type"))
      ruleConfig <- RuleConfig.fromFields(ruleType, rule).toRight(UnknownFeedFilterRule(rule))
    } yield ruleConfig).toValidated

    (name.validNel[ConfigParseError] |@|
      src.validNel[ConfigParseError] |@|
      ExtractorChoice.fromString(extract).toValidNel(UnknownFeedItemExtractor(extract)) |@|
      validatedRule.toValidatedNel).map(FeedConfig.apply _)
  }
}

object YamlFeedConfig extends DefaultYamlProtocol {
  implicit object UriYamlFormat extends YamlFormat[URI] {
    def write(u: URI) = YamlString(u.toString)
    def read(value: YamlValue) = value match {
      case YamlString(s) =>
        try { new URI(s) } catch {
          case _: Throwable => deserializationError(s"Expected valid URI, but got $s")
        }
      case y =>
        deserializationError(s"Expected Int as YamlNumber, but got $y")
    }
  }

  implicit val format: YamlFormat[YamlFeedConfig] = yamlFormat4(YamlFeedConfig.apply)

  def parse(yaml: String): Either[ConfigParseError, List[YamlFeedConfig]] =
    Either
      .catchOnly[DeserializationException](yaml.parseYaml.convertTo[List[YamlFeedConfig]])
      .left
      .map(t => YamlParseError(t))
}
