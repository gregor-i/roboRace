package gameLogic.gameUpdate

import gameLogic.action.ActionSlots
import gameLogic.{AllPlayersFinished, EventLog, GameFinished, GameRunning, GameState, Logged, PlayerFinished, Robot, RobotReset, Robots}

object ScenarioEffects {

  def afterAction(game: GameRunning): Logged[GameRunning] = {
    def resetRobot(player: String, robot: Robot)(robots: Robots): Logged[Robots] = {
      val index = game.players.zipWithIndex.find(_._1 == player).get._2
      val initial = game.scenario.initialRobots(index)
      for {
        clearedInitial <- PushRobots(initial.position, initial.direction, robots)
        resettedFallen <- (clearedInitial + (player -> initial)).log(RobotReset(player, robot, initial))
      } yield resettedFallen
    }

    val actions = game.robots.filter {
      case (_, robot) => robot.position.x >= game.scenario.width ||
        robot.position.x < 0 ||
        robot.position.y >= game.scenario.height ||
        robot.position.y < 0
    }.map { case (player, robot) =>
      resetRobot(player, robot)(_)
    }

    for {
      updatedRobots <- Logged.flatMapFold[Robots, EventLog](Logged.pure(game.robots))(actions)
    } yield game.copy(robots = updatedRobots)
  }

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
        val index = game.players.zipWithIndex.collect { case (`player`, index) => index }.head
        val initial = game.scenario.initialRobots(index)
        for {
          clearedInitial <- PushRobots(initial.position, initial.direction, game.robots)
          resettedFallen <- (clearedInitial + (player -> initial)).log(RobotReset(player, robot, initial))
          resettedActions = game.robotActions + (player -> ActionSlots.emptyActionSet)
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
          players = game.players.filter(_ != player),
          finishedPlayers = game.finishedPlayers :+ playerFinished,
          robots = game.robots - player
        ).log(playerFinished)
    }
  }
}
