package gameLogic
package command

import gameLogic.action._
import gameLogic.eventLog._

object Cycle extends GameUpdate {
  def apply(gameState: GameState): LoggedGameState = gameState match {
    case g: GameRunning if g.robotActions.keySet == g.players.toSet =>
      val playerActions = g.players.map { player =>
        val robotAction = g.robotActions(player)
        ApplyAction(player, robotAction)
      }

      for {
        game <- g.log(AllPlayerDefinedActions)
        afterPlayerActions <- playerActions.foldLeft[Logged[GameRunning, EventLog]](Logged.pure(game))((state, action) => state.flatMap(action.apply))
        nextCycle <- afterPlayerActions.copy(cycle = g.cycle + 1, robotActions = Map.empty).log(PlayerActionsExecuted(game.cycle + 1))
      } yield nextCycle
    case _ => Logged.pure(gameState)
  }

}

case class ApplyAction(player: String, action: Action) {
  def apply(gameState: GameRunning): Logged[GameRunning, EventLog] = {
    for {
      robot <- gameState.robots(player).log(RobotAction(player, action))
      nextRobotState <- action match {
        case TurnLeft => robot.copy(direction = robot.direction.left).log(RobotDirectionTransition(player, robot.direction, robot.direction.left))
        case TurnRight => robot.copy(direction = robot.direction.right).log(RobotDirectionTransition(player, robot.direction, robot.direction.right))
        case MoveForward => robot.copy(position = robot.position.move(robot.direction)).log(RobotPositionTransition(player, robot.position, robot.position.move(robot.direction)))
        case MoveBackward => robot.copy(position = robot.position.move(robot.direction.back)).log(RobotPositionTransition(player, robot.position, robot.position.move(robot.direction.back)))
      }
      // todo: push other robots. trigger field like fall into pit or from board.
    } yield gameState.copy(robots = gameState.robots.updated(player, nextRobotState))
  }
}