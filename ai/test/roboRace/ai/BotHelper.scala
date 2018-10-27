package roboRace.ai

import gameLogic.{Game, Player, Scenario}
import helper.GameUpdateHelper
import org.scalatest.Matchers

trait BotHelper extends GameUpdateHelper {
  _: Matchers =>
  val bot = "bot"

  def botInstructions(bot: Bot, player: String): Game => Game = game => {
    val p = game.players.find(_.name == player).get
    val chosen = bot.apply(game.scenario, game.players.filter(_.name != player).map(_.robot))(p.robot, p.instructionOptions)
    Game.player(player).composeLens(Player.instructionSlots).set(chosen.map(Some.apply))(game)
  }

  def botFinishesGame(bot: Bot, maxCycles: Int, scenario: Scenario): Boolean = {
    val g = sequenceWithAutoCycle(createGame(scenario)("bot"))(
      Seq.fill(maxCycles)(botInstructions(bot, "bot")): _*
    )
    g.players.head.finished.isDefined
  }
}
