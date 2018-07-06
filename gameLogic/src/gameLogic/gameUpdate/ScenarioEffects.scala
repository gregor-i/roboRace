package gameLogic.gameUpdate

import gameLogic.{FinishedStatistic, Game, PlayerFinished, RunningPlayer}

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
        val index = player.index
        val initial = game.scenario.initialRobots(index)
        val clearedInitial = MoveRobots.pushRobots(initial.position, initial.direction, game)
        val resettedFallen = Events.reset(player, initial)(clearedInitial)
        fallenRobots(resettedFallen)
    }
  }

  def afterCycle(game: Game): Game = {
    game.players.find {
      player => player.robot.position == game.scenario.targetPosition && player.finished.isEmpty
    } match {
      case None => game
      case Some(player) =>
        val stats = FinishedStatistic(rank = game.players.count(_.finished.isDefined) + 1, cycle = game.cycle, rageQuitted = false)
        val playerFinished = PlayerFinished(player.name, stats)

        (Game.player(player.name) composeLens RunningPlayer.finished)
          .set(Some(stats))
          .apply(game)
          .addLogs(playerFinished)
    }
  }
}
