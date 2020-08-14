package logic.gameUpdate

import entities._
import logic.{Lenses, PlayerLenses, State}

object ScenarioEffects {
  def beforeCycle(game: Game): Game = TrapEffects.applyAll(game)

  def afterCycle(game: Game): Game = finishPlayers(game)

  def afterMoveAction(game: Game): Game = fallenRobots(game)

  private def finishPlayers(game: Game): Game = {
    val updates = for (player <- Lenses.runningPlayers.getAll(game)) yield {
      val targetIndex = player.currentTarget
      val target      = game.scenario.targets(targetIndex)
      if (player.robot.position == target) {
        if (targetIndex == game.scenario.targets.length - 1) {
          val p = FinishedPlayer(
            index = player.index,
            id = player.id,
            rank = game.players.count(_.isInstanceOf[FinishedPlayer]) + 1,
            cycle = game.cycle
          )
          Seq(
            Lenses.player(player.id).set(p),
            Lenses.log(PlayerFinished(player.index, player.robot))
          )
        } else {
          Seq(
            Lenses.player(player.id).set(player.copy(currentTarget = player.currentTarget + 1)),
            Lenses.log(PlayerReachedTarget(player.index))
          )
        }
      } else {
        Seq.empty
      }
    }
    State.all(updates.flatten)(game)
  }

  private def fallenRobots(game: Game): Game = {
    game.players.find {
      case player: RunningPlayer =>
        val robot = player.robot
        robot.position.x >= game.scenario.width ||
        robot.position.x < 0 ||
        robot.position.y >= game.scenario.height ||
        robot.position.y < 0 ||
        game.scenario.pits.contains(robot.position)
      case _ => false
    } match {
      case Some(player: RunningPlayer) =>
        val initial = game.scenario.initialRobots(player.index)
        val clearedInitial = MoveRobots.pushRobots(initial.position, initial.direction, game) match {
          case Some(robotPushed) => Events.move(robotPushed)(game)
          case None              => game
        }
        fallenRobots(Events.reset(player, initial)(clearedInitial))
      case _ => game
    }
  }
}
