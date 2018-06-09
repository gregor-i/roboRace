package gameLogic
package gameUpdate

sealed trait Command extends (GameState => CommandResponse) {
  def apply(gameState: GameState): CommandResponse = gameState match {
    case InitialGame => ifInitial
    case g: GameStarting => ifStarting(g)
    case g: GameRunning => ifRunning(g)
    case g: GameFinished => ifFinished(g)
  }

  def ifInitial: CommandResponse = CommandRejected(WrongState)
  def ifStarting: GameStarting => CommandResponse = _ => CommandRejected(WrongState)
  def ifRunning: GameRunning => CommandResponse = _ => CommandRejected(WrongState)
  def ifFinished: GameFinished => CommandResponse = _ => CommandRejected(WrongState)
}

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason) extends CommandResponse
case class CommandAccepted(newState: GameState) extends CommandResponse

case class DefineScenario(scenario: GameScenario) extends Command {
  override def ifInitial: CommandResponse =
    CommandAccepted(GameStarting(scenario, Nil))
}

case class RegisterForGame(playerName: String) extends Command {
  override def ifStarting: GameStarting => CommandResponse = {
    case g if g.players.exists(_.name == playerName) =>
      CommandRejected(PlayerAlreadyRegistered)
    case g if g.players.size >= g.scenario.initialRobots.size =>
      CommandRejected(TooMuchPlayersRegistered)
    case g =>
      CommandAccepted(GameStarting.players.modify(players => players :+ StartingPlayer(players.size, playerName, ready = false))(g))
  }
}

case class ReadyForGame(playerName: String) extends Command {
  override def ifStarting: GameStarting => CommandResponse = {
    case g if !g.players.exists(_.name == playerName) =>
      CommandRejected(PlayerNotFound)
    case g =>
      CommandAccepted((GameStarting.player(playerName) composeLens StartingPlayer.ready).set(true)(g))
  }
}

case class ChooseInstructions(player: String, cycle: Int, instructions: Seq[Int]) extends Command {
  override def ifRunning: GameRunning => CommandResponse = {
    case g if g.cycle != cycle =>
      CommandRejected(WrongCycle)
    case g if !g.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case g if instructions.size != Constants.instructionsPerCycle ||
      instructions.distinct.size != Constants.instructionsPerCycle ||
      instructions.forall(i => 0 > i || i > Constants.instructionOptionsPerCycle) =>
      CommandRejected(InvalidActionChoice)
    case g =>
      CommandAccepted(GameRunning.player(player).modify(p => p.copy(instructions = instructions.map(p.instructionOptions)))(g))
  }
}
