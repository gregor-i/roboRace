package gameLogic
package gameUpdate

sealed trait Command {
  def apply(player: String): Game => CommandResponse
}

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason) extends CommandResponse
case class CommandAccepted(newState: Game) extends CommandResponse

case object RegisterForGame extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if game.cycle != 0 =>
      CommandRejected(WrongCycle)
    case game if game.players.exists(_.name == player) =>
      CommandRejected(PlayerAlreadyRegistered)
    case game if game.players.size >= game.scenario.initialRobots.size =>
      CommandRejected(TooMuchPlayersRegistered)
    case game =>
      val newPlayer = RunningPlayer(index = game.players.size,
        name = player,
        robot = game.scenario.initialRobots(game.players.size),
        instructions = Seq.empty,
        instructionOptions = DealOptions.initial,
        finished = None
      )
      CommandAccepted(Game.players.modify(players => players :+ newPlayer)(game))
  }
}

case object DeregisterForGame extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)

    case game if game.cycle == 0 =>
      CommandAccepted(Game.players.modify(_.filter(_.name != player).zipWithIndex.map{case (player, index) => player.copy(index =index)})(game))
    case game =>
      CommandAccepted((Game.player(player) composeLens RunningPlayer.finished)
        .set(Some(FinishedStatistic(game.players.count(_.finished.isEmpty), game.cycle, true)))(game))
  }
}

case class ChooseInstructions(cycle: Int, instructions: Seq[Int]) extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if instructions.size != Constants.instructionsPerCycle ||
      instructions.distinct.size != Constants.instructionsPerCycle ||
      instructions.forall(i => 0 > i && i > Constants.instructionOptionsPerCycle) =>
      CommandRejected(InvalidActionChoice)
    case game =>
      CommandAccepted(Game.player(player).modify(p => p.copy(instructions = instructions.map(p.instructionOptions)))(game))
  }
}

