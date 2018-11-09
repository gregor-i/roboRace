package roboRace.ai

import helper.GameUpdateHelper
import org.scalatest.Matchers

trait BotHelper extends GameUpdateHelper {
  _: Matchers =>
  val bot = "bot"
}
