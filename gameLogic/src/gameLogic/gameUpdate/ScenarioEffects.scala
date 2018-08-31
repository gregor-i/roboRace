package gameLogic.gameUpdate

import gameLogic.{FinishedStatistic, Game, PlayerFinished, Player}

object ScenarioEffects {

  def afterMoveAction(game: Game): Game =
    fallenRobots(game)

  private def fallenRobots(game: Game): Game = {
    val firstFallenPlayer = game.players.find {
      player =>
        val robot = player.robot
        robot.position.x >= game.scenario.width ||
          robot.position.x < 0 ||
          robot.position.y >= game.scenario.height ||
          robot.position.y < 0 ||
          game.scenario.pits.contains(robot.position)
    }

    firstFallenPlayer match {
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

  def afterCycle(game: Game): Game = {
    game.players.find {
      player => player.robot.position == game.scenario.targetPosition && player.finished.isEmpty
    } match {
      case None => game
      case Some(player) =>
        val stats = FinishedStatistic(rank = game.players.count(_.finished.isDefined) + 1, cycle = game.cycle, rageQuitted = false)

        (Game.player(player.name) composeLens Player.finished)
          .set(Some(stats))
          .apply(game)
          .log(PlayerFinished(player.index))
    }
  }
}
