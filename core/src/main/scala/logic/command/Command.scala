package logic
package command

import entities._
import logic.command
import logic.gameUpdate.DealOptions

sealed trait Command

case class RegisterForGame(index: Int)                     extends Command
case object DeregisterForGame                              extends Command
case class SetInstructions(instructions: Seq[Instruction]) extends Command
case object ResetInstruction                               extends Command

object Command {
  def apply(command: Command, player: String)(game: Game): CommandResponse = command match {
    case RegisterForGame(index)        => registerForGame(index)(player)(game)
    case DeregisterForGame             => deregisterForGame(player)(game)
    case SetInstructions(instructions) => setInstructions(instructions)(player)(game)
    case ResetInstruction              => resetInstruction(player)(game)
  }

  def registerForGame(index: Int)(playerId: String): Game => CommandResponse = {
    case game if game.cycle != 0 =>
      Left(WrongCycle)
    case game if game.players.exists(_.id == playerId) =>
      Left(PlayerAlreadyRegistered)
    case game if !game.scenario.initialRobots.indices.contains(index) =>
      Left(InvalidIndex)
    case game if game.players.exists(_.index == index) =>
      Left(RobotAlreadyTaken)
    case game =>
      val newPlayer = RunningPlayer(
        index = index,
        id = playerId,
        currentTarget = 0,
        robot = game.scenario.initialRobots(index),
        instructionSlots = Seq.empty,
        instructionOptions = DealOptions.initial
      )
      Right(
        State.sequence(
          Game.players.modify(players => players :+ newPlayer),
          _.log(PlayerJoinedGame(newPlayer.index, newPlayer.robot))
        )(game)
      )
  }

  def deregisterForGame(playerId: String)(game: Game): CommandResponse =
    Lenses.player(playerId).headOption(game) match {
      case None =>
        Left(PlayerNotFound)
      case Some(_: QuittedPlayer) =>
        Left(PlayerAlreadyQuitted)
      case Some(_: FinishedPlayer) =>
        Left(PlayerAlreadyFinished)

      case Some(player: RunningPlayer) if game.cycle == 0 =>
        Right(
          State.sequence(
            Game.players.modify(_.filter(_ != player)),
            _.log(PlayerQuitted(player.index, player.robot))
          )(game)
        )

      case Some(player: RunningPlayer) =>
        Right(
          State.sequence(
            Lenses.player(playerId).modify(p => QuittedPlayer(p.index, p.id)),
            _.log(PlayerQuitted(player.index, player.robot))
          )(game)
        )
    }

  def setInstructions(instructions: Seq[Instruction])(playerId: String)(game: Game): CommandResponse =
    Lenses.player(playerId).headOption(game) match {
      case None =>
        Left(PlayerNotFound)
      case Some(_: FinishedPlayer) =>
        Left(PlayerAlreadyFinished)
      case _ if instructions.size != Constants.instructionsPerCycle =>
        Left(InvalidActionChoice)
      case Some(player: RunningPlayer)
          if !Instruction.instructions.forall(
            instr => instructions.count(_ == instr) <= player.instructionOptions.find(_.instruction == instr).fold(0)(_.count)
          ) =>
        Left(InvalidActionChoice)
      case _ =>
        Right(Lenses.instructionSlots(playerId).set(instructions)(game))
    }

  def resetInstruction(playerId: String)(game: Game): CommandResponse =
    Lenses.player(playerId).headOption(game) match {
      case None =>
        Left(PlayerNotFound)
      case Some(_: QuittedPlayer) =>
        Left(PlayerAlreadyFinished)
      case Some(_: FinishedPlayer) =>
        Left(PlayerAlreadyFinished)
      case Some(_: RunningPlayer) =>
        Right(Lenses.instructionSlots(playerId).set(Seq.empty)(game))
    }
}
