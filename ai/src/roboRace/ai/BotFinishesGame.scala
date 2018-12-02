package roboRace.ai

import gameLogic.command.{CommandAccepted, CreateGame}
import gameLogic.gameUpdate.Cycle
import gameLogic.{Game, Player, Scenario, State}

object BotFinishesGame {
  def sequenceWithAutoCycle(state: Game)(fs: (Game => Game)*): Game =
    State.sequence(fs.map(_ andThen Cycle): _*)(state)

  def createGame(scenario: Scenario)(player: String): Game =
    CreateGame(scenario)(player).asInstanceOf[CommandAccepted].newState

  def botInstructions(bot: Bot, player: String): Game => Game = game => {
    val p = game.players.find(_.name == player).get
    val chosen = bot(game.scenario, game.players.filter(_.name != player).map(_.robot), p)
    Game.player(player).composeLens(Player.instructionSlots).set(chosen.map(Some.apply))(game)
  }

  def apply(maxCycles: Int, scenario: Scenario)(bot: Bot): Boolean = {
    val g = sequenceWithAutoCycle(createGame(scenario)("bot"))(
      Seq.fill(maxCycles)(botInstructions(bot, "bot")): _*
    )
    g.players.head.finished.isDefined
  }
}