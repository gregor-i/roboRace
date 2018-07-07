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
      val newPlayer = Player(index = game.players.size,
        name = player,
        robot = game.scenario.initialRobots(game.players.size),
        instructionSlots = Instruction.emptySlots,
        instructionOptions = DealOptions.initial,
        finished = None
      )
      CommandAccepted(
        Game.players.modify(players => players :+ newPlayer)(game)
          .log(PlayerJoinedGame(player))
      )
  }
}

case object DeregisterForGame extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)

    case game if game.cycle == 0 =>
      CommandAccepted(
        Game.players.modify(_.filter(_.name != player).zipWithIndex.map{case (player, index) => player.copy(index =index)})(game)
        .log(PlayerRageQuitted(player))
      )
    case game =>
      CommandAccepted(
        (Game.player(player) composeLens Player.finished)
        .set(Some(FinishedStatistic(game.players.count(_.finished.isEmpty), game.cycle, true)))(game)
          .log(PlayerRageQuitted(player))
      )

  }
}

case class SetInstruction(cycle: Int, slot: Int, instruction: Int) extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if 0 > slot || slot >= Constants.instructionsPerCycle =>
      CommandRejected(InvalidSlot)
    case game if 0 > instruction || instruction >= Constants.instructionOptionsPerCycle =>
      CommandRejected(InvalidActionChoice)
    case game if game.players.find(_.name == player).get.instructionSlots.contains(Some(instruction)) =>
      CommandRejected(ActionAlreadyUsed)
    case game =>
      CommandAccepted((Game.player(player) composeLens Player.instructionSlots).modify(_.updated(slot, Some(instruction)))(game))
  }
}

case class ResetInstruction(cycle: Int, slot: Int) extends Command {
  def apply(player: String): Game => CommandResponse = {
    case game if game.cycle != cycle =>
      CommandRejected(WrongCycle)
    case game if !game.players.exists(_.name == player) =>
      CommandRejected(PlayerNotFound)
    case game if game.players.find(_.name == player).exists(_.finished.isDefined) =>
      CommandRejected(PlayerAlreadyFinished)
    case game if 0 > slot || slot >= Constants.instructionsPerCycle =>
      CommandRejected(InvalidSlot)
    case game =>
      CommandAccepted((Game.player(player) composeLens Player.instructionSlots).modify(_.updated(slot, None))(game))
  }
}

