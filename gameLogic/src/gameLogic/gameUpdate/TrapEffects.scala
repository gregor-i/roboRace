package gameLogic
package gameUpdate

import gameEntities._

object TrapEffects {

  def applyAll(game: Game): Game =
    State.all(
      for {
        trap <- game.scenario.traps
        player <- game.players.find(_.robot.position == trap.position)
      } yield apply(trap, player)
    )(game)

  def afterMove(pushed: RobotPushed)(game: Game): Game = {
    val after1 = game.scenario.traps.find(_.position == pushed.to) match {
      case Some(trap) => apply(trap, game.players.find(_.index == pushed.player.index).get)(game)
      case None => game
    }
    pushed.push match {
      case Some(push) => afterMove(push)(after1)
      case None => after1
    }
  }

  def apply(trap: Trap, player: Player): Game => Game =
    State.sequence(
      trap match {
        case _: TurnRightTrap =>
          Events.turn(player, Direction.turnRight(player.robot.direction))
        case _: TurnLeftTrap =>
          Events.turn(player, Direction.turnLeft(player.robot.direction))
        case _: StunTrap =>
          Events.stun(player)
      },
      Lenses.log(TrapEffect(player.index, trap))
    )
}
