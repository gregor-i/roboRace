package gameLogic.gameUpdate

import gameLogic.{FinishedStatistic, GameRunning, Logged, PlayerFinished, RobotReset}

object ScenarioEffects {

  def afterMoveAction(game: GameRunning): Logged[GameRunning] =
    fallenRobots(game)

  private def fallenRobots(game: GameRunning): Logged[GameRunning] = {
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
          clearedInitial <- PushRobots(initial.position, initial.direction, game)
          resettedFallen <- clearedInitial.copy(players = clearedInitial.players.map(p => if (p.name == player.name) p.copy(robot = initial, actions = Seq.empty) else p)).log(RobotReset(player.name, player.robot, initial))
          recursion <- fallenRobots(resettedFallen)
        } yield recursion

    }
  }

  def afterCycle(game: GameRunning): Logged[GameRunning] = {
    game.players.find {
      player => player.robot.position == game.scenario.targetPosition && player.finished.isEmpty
    } match {
      case None => Logged.pure(game)
      case Some(player) =>
        val stats = FinishedStatistic(rank = game.players.count(_.finished.isDefined) + 1, cycle = game.cycle)
        val playerFinished = PlayerFinished(player.name, stats)
        val remainingPlayers = game.players.filter(_ != player)

        game.copy(
          players = game.players.map(p => if (p.name == player.name) p.copy(finished = Some(stats)) else p)
        ).log(playerFinished)
    }
  }
}
