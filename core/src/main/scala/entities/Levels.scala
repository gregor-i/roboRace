package entities

import roborace.macros.StaticContent

import scala.util.chaining._

import io.circe.parser
import io.circe.generic.auto._

object Levels {
  def parse(jsonString: String): Scenario =
    jsonString
      .pipe(parser.decode[Scenario])
      .getOrElse(throw new Exception("json scenario could not be parsed"))

  val level1 = StaticContent("core/src/main/json/level1.json").pipe(parse)
}
