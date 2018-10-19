package gameLogic.gameUpdate

import gameLogic._

object ScenarioEffects {
  def beforeCycle(game: Game): Game = TrapEffects.applyAll(game)

  def afterCycle(game: Game): Game = finishPlayers(game)

  def afterMoveAction(game: Game): Game = fallenRobots(game)

  private def finishPlayers(game: Game): Game = {
    game.players.find {
      player => player.robot.position == game.scenario.targetPosition && player.finished.isEmpty
    } match {
      case None => game
      case Some(player) =>
        val stats = FinishedStatistic(
          rank = game.players.count(_.finished.isDefined) + 1,
          cycle = game.cycle,
          rageQuitted = false)

        State.sequence(
          (Game.player(player.name) composeLens Player.finished).set(Some(stats)),
          _.log(PlayerFinished(player.index, player.robot))
        )(game)
    }
  }

  def isPit(scenario: Scenario, position: Position): Boolean =
    position.x >= scenario.width ||
      position.x < 0 ||
      position.y >= scenario.height ||
      position.y < 0 ||
      scenario.pits.contains(position)

  private def fallenRobots(game: Game): Game = {
    game.players.find {
      player => isPit(game.scenario, player.robot.position)
    } match {
      case None         => game
      case Some(player) =>
        val initial = game.scenario.initialRobots(player.index)
        val clearedInitial = MoveRobots.pushRobots(initial.position, initial.direction, game) match {
          case Some(robotPushed) => Events.move(robotPushed)(game)
          case None => game
        }
        fallenRobots(Events.reset(player, initial)(clearedInitial))
    }
  }
}
