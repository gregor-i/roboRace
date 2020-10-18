package roborace.frontend.service

import api.{Entity, GameResponse, WithId}
import entities.{Constants, Instruction, RunningPlayer, Scenario}
import logic.command.{Command, ResetInstruction, SetInstructions}
import roborace.frontend.Context
import roborace.frontend.pages.multiplayer.game.GameState
import roborace.frontend.pages.multiplayer.lobby.LobbyState
import roborace.frontend.toasts.Syntax.{withSuccessToast, withWarningToast}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Actions {
  def createGame(scenario: Scenario, index: Int): Future[GameResponse] =
    withSuccessToast("Starting Game", "Game started") {
      Service.postGame(scenario, index)
    }

  def deleteGame(gameResponse: GameResponse)(implicit context: Context[LobbyState]): Future[Unit] =
    withWarningToast("Deleting Game", "Game deleted") {
      Service.deleteGame(gameResponse.id)
    }.map { _ =>
      context.update(LobbyState.games.modify(_.filter(_.id != gameResponse.id))(context.local))
    }

  def sendCommand(command: Command)(implicit context: Context[GameState]): Unit =
    withSuccessToast("Sending Instructions to Server", "Instructions send to Server") {
      Service.postCommand(context.local.game.id, command)
    }.map { gameReponse =>
      val newState = GameState.game.set(gameReponse)(context.local)
      context.update(GameState.clearSlots(context.local, newState))
    }

  def placeInstruction(instruction: Instruction, slot: Int)(implicit context: Context[GameState]): Unit = {
    val newState = GameState.slots.modify(_ + (slot -> instruction))(context.local)
    if (newState.slots.size == Constants.instructionsPerCycle) {
      // todo: this is hacky
      val newContext = Context(newState, context.global, context.update).asInstanceOf[Context[GameState]]
      sendCommand(SetInstructions(newState.slots.toSeq.sortBy(_._1).map(_._2)))(newContext)
    } else {
      val nextFreeSlot = (0 until Constants.instructionsPerCycle)
        .map(i => (i + newState.focusedSlot) % Constants.instructionsPerCycle)
        .find(i => !newState.slots.contains(i))
        .getOrElse(0)
      context.update(GameState.focusedSlot.set(nextFreeSlot)(newState))
    }
  }

  def unsetInstruction(slot: Int)(implicit context: Context[GameState]): Unit = {
    val newState = GameState.slots.modify(_ - slot)(context.local)
    newState.game.you match {
      case Some(you: RunningPlayer) if you.instructionSlots.nonEmpty =>
        sendCommand(ResetInstruction)
      case _ =>
        context.update(newState)
    }
  }

  def saveScenario(scenario: Entity[Scenario]): Future[WithId[Entity[Scenario]]] =
    withSuccessToast("Saving Scenario", "Scenario saved") {
      Service.postScenario(scenario)
    }

  def deleteScenario(scenario: WithId[Entity[Scenario]])(implicit context: Context[LobbyState]): Unit =
    withWarningToast("Deleting Scenario", "Scenario deleted") {
      Service.deleteScenario(scenario)
    }.map { _ =>
      val newState = LobbyState.scenarios.modify(_.filter(_ != scenario))(context.local)
      context.update(newState)
    }
}
