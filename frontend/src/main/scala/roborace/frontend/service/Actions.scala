package roborace.frontend.service

import api.{GameResponse, ScenarioPost, ScenarioResponse}
import entities.{Constants, Instruction, RunningPlayer, Scenario}
import logic.command.{Command, ResetInstruction, SetInstructions}
import roborace.frontend.FrontendState
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

  def deleteGame(gameResponse: GameResponse)(implicit state: LobbyState, update: FrontendState => Unit): Future[Unit] =
    withWarningToast("Deleting Game", "Game deleted") {
      Service.deleteGame(gameResponse.id)
    }.map { _ =>
      update(LobbyState.games.modify(_.filter(_.id != gameResponse.id))(state))
    }

  def sendCommand(command: Command)(implicit gameState: GameState, update: FrontendState => Unit): Unit =
    withSuccessToast("Sending Instructions to Server", "Instructions send to Server") {
      Service.postCommand(gameState.game.id, command)
    }.map { gameReponse =>
      val newState = GameState.game.set(gameReponse)(gameState)
      update(GameState.clearSlots(gameState, newState))
    }

  def placeInstruction(instruction: Instruction, slot: Int)(implicit state: GameState, update: FrontendState => Unit): Unit = {
    val newState = GameState.slots.modify(_ + (slot -> instruction))(state)
    if (newState.slots.size == Constants.instructionsPerCycle) {
      sendCommand(SetInstructions(newState.slots.toSeq.sortBy(_._1).map(_._2)))
    } else {
      val nextFreeSlot = (0 until Constants.instructionsPerCycle)
        .map(i => (i + newState.focusedSlot) % Constants.instructionsPerCycle)
        .find(i => !newState.slots.contains(i))
        .getOrElse(0)
      update(GameState.focusedSlot.set(nextFreeSlot)(newState))
    }
  }

  def unsetInstruction(slot: Int)(implicit state: GameState, update: FrontendState => Unit): Unit = {
    val newState = GameState.slots.modify(_ - slot)(state)
    newState.game.you match {
      case Some(you: RunningPlayer) if you.instructionSlots.nonEmpty =>
        sendCommand(ResetInstruction)
      case _ =>
        update(newState)
    }
  }

  def saveScenario(scenario: ScenarioPost): Future[ScenarioResponse] =
    withSuccessToast("Saving Scenario", "Scenario saved") {
      Service.postScenario(scenario)
    }

  def deleteScenario(scenario: ScenarioResponse)(implicit state: LobbyState, update: FrontendState => Unit): Unit =
    withWarningToast("Deleting Scenario", "Scenario deleted") {
      Service.deleteScenario(scenario)
    }.map { _ =>
      val newState = LobbyState.scenarios.modify(_.filter(_ != scenario))(state)
      update(newState)
    }
}
