package gameLogic.gameUpdate

import gameLogic.{FinishedStatistic, Game, Logged, PlayerFinished, RobotReset, RunningPlayer}

object ScenarioEffects {

  def afterMoveAction(game: Game): Logged[Game] =
    fallenRobots(game)

  private def fallenRobots(game: Game): Logged[Game] = {
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
      case None => Logged.pure(game)
      case Some(player) =>
        val index = player.index
        val initial = game.scenario.initialRobots(index)
        for {
          clearedInitial <- MoveRobots.pushRobots(initial.position, initial.direction, game)
          resettedFallen <- Events.reset(player, initial)(clearedInitial)
          recursion <- fallenRobots(resettedFallen)
        } yield recursion

    }
  }

  def afterCycle(game: Game): Logged[Game] = {
    game.players.find {
      player => player.robot.position == game.scenario.targetPosition && player.finished.isEmpty
    } match {
      case None => Logged.pure(game)
      case Some(player) =>
        val stats = FinishedStatistic(rank = game.players.count(_.finished.isDefined) + 1, cycle = game.cycle, rageQuitted = false)
        val playerFinished = PlayerFinished(player.name, stats)

        (Game.player(player.name) composeLens RunningPlayer.finished)
          .set(Some(stats))
          .apply(game)
          .log(playerFinished)
    }
  }
}
