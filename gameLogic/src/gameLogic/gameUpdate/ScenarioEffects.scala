package gameLogic.gameUpdate

import gameLogic.{GameRunning, Logged, PlayerFinished, RobotReset}

object ScenarioEffects {

  def afterAction(game: GameRunning): Logged[GameRunning] =
    fallenRobots(game)

  private def fallenRobots(game: GameRunning): Logged[GameRunning] = {
    val firstFallenRobot = game.robots.find {
      case (_, robot) => robot.position.x >= game.scenario.width ||
        robot.position.x < 0 ||
        robot.position.y >= game.scenario.height ||
        robot.position.y < 0
    }

    firstFallenRobot match {
      case None => Logged.pure(game)
      case Some((player, robot)) =>
        val index = game.players.zipWithIndex.find(_._1 == player).get._2
        val initial = game.scenario.initialRobots(index)
        for {
          clearedInitial <- PushRobots(initial.position, initial.direction, game.robots)
          resettedFallen <- (clearedInitial + (player -> initial)).log(RobotReset(player, robot, initial))
          resettedActions = game.robotActions + (player -> Seq.empty)
          recursion <- fallenRobots(game.copy(robots = resettedFallen, robotActions = resettedActions))
        } yield recursion

    }
  }

  def afterCycle(game: GameRunning): Logged[GameRunning] = {
    game.robots.find(_._2.position == game.scenario.targetPosition) match {
      case None => Logged.pure(game)
      case Some((player, _)) =>
        val playerFinished = PlayerFinished(player, game.finishedPlayers.size + 1, game.cycle)
        val remainingPlayers = game.players.filter(_ != player)

        game.copy(
          finishedPlayers = game.finishedPlayers :+ playerFinished,
          robots = game.robots + (player -> game.robots(player).copy(finished = true))
        ).log(playerFinished)
    }
  }
}
