package gameLogic
package command

import gameEntities._
import gameLogic.gameUpdate.DealOptions

object Command {
  def apply(command: Command, player: String)(game: Game): CommandResponse = command match {
    case RegisterForGame(index)        => registerForGame(index)(player)(game)
    case DeregisterForGame             => deregisterForGame(player)(game)
    case SetInstructions(instructions) => setInstructions(instructions)(player)(game)
    case ResetInstruction              => resetInstruction(player)(game)
  }

  def registerForGame(index: Int)(playerId: String): Game => CommandResponse = {
    case game if game.cycle != 0 =>
      CommandRejected(WrongCycle)
    case game if game.players.exists(_.id == playerId) =>
      CommandRejected(PlayerAlreadyRegistered)
    case game if !game.scenario.initialRobots.indices.contains(index) =>
      CommandRejected(InvalidIndex)
    case game if game.players.exists(_.index == index) =>
      CommandRejected(RobotAlreadyTaken)
    case game =>
      val newPlayer = RunningPlayer(
        index = index,
        id = playerId,
        currentTarget = 0,
        robot = game.scenario.initialRobots(index),
        instructionSlots = Seq.empty,
        instructionOptions = DealOptions.initial
      )
      CommandAccepted(
        State.sequence(
          Lenses.players.modify(players => players :+ newPlayer),
          Lenses.log(PlayerJoinedGame(newPlayer.index, newPlayer.robot))
        )(game)
      )
  }

  def deregisterForGame(playerId: String)(game: Game): CommandResponse =
    Lenses.player(playerId).headOption(game) match {
      case None =>
        CommandRejected(PlayerNotFound)
      case Some(_: QuittedPlayer) =>
        CommandRejected(PlayerAlreadyQuitted)
      case Some(_: FinishedPlayer) =>
        CommandRejected(PlayerAlreadyFinished)

      case Some(player: RunningPlayer) if game.cycle == 0 =>
        CommandAccepted(
          State.sequence(
            Lenses.players.modify(_.filter(_ != player)),
            Lenses.log(PlayerQuitted(player.index, player.robot))
          )(game)
        )

      case Some(player: RunningPlayer) =>
        CommandAccepted(
          State.sequence(
            Lenses.player(playerId).modify(p => QuittedPlayer(p.index, p.id)),
            Lenses.log(PlayerQuitted(player.index, player.robot))
          )(game)
        )
    }

  def setInstructions(instructions: Seq[Instruction])(playerId: String)(game: Game): CommandResponse =
    Lenses.player(playerId).headOption(game) match {
      case None =>
        CommandRejected(PlayerNotFound)
      case Some(_: FinishedPlayer) =>
        CommandRejected(PlayerAlreadyFinished)
      case _ if instructions.size != Constants.instructionsPerCycle =>
        CommandRejected(InvalidActionChoice)
      case Some(player: RunningPlayer)
          if !Instruction.instructions.forall(
            instr => instructions.count(_ == instr) <= player.instructionOptions.find(_.instruction == instr).fold(0)(_.count)
          ) =>
        CommandRejected(InvalidActionChoice)
      case _ =>
        CommandAccepted(Lenses.instructionSlots(playerId).set(instructions)(game))
    }

  def resetInstruction(playerId: String)(game: Game): CommandResponse =
    Lenses.player(playerId).headOption(game) match {
      case None =>
        CommandRejected(PlayerNotFound)
      case Some(_: QuittedPlayer) =>
        CommandRejected(PlayerAlreadyFinished)
      case Some(_: FinishedPlayer) =>
        CommandRejected(PlayerAlreadyFinished)
      case Some(_: RunningPlayer) =>
        CommandAccepted(Lenses.instructionSlots(playerId).set(Seq.empty)(game))
    }
}
