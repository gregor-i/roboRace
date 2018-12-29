package frontend.game

import com.raquo.snabbdom.simple.VNode
import com.raquo.snabbdom.simple.attrs.id
import com.raquo.snabbdom.simple.events.{onClick, onDblClick}
import com.raquo.snabbdom.simple.implicits._
import com.raquo.snabbdom.simple.props.{className, src}
import com.raquo.snabbdom.simple.tags._
import frontend.Main
import frontend.components.{Fab, Images}
import frontend.gameBoard.{Animation, RenderGame}
import frontend.util.{Dynamic, Ui}
import gameEntities._
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

object GameUi extends Ui {
  def apply(gameState: GameState, rerender: GameState => Unit): VNode = {
    val replayFab = Fab("fab-left-1", Images.iconReplayAnimation,
      onClick := (() =>
        Dynamic(dom.document.querySelector(".game-board svg")).setCurrentTime(
          if (gameState.game.cycle == 0)
            0
          else
            Animation.eventSequenceDuration(gameState.game.events.takeWhile {
              case s: StartCycleEvaluation => s.cycle != gameState.game.cycle - 1
              case _                       => true
            })
        )
        ),
      onDblClick := (() =>
        Dynamic(dom.document.querySelector(".game-board svg")).setCurrentTime(0)
        )
    )
    val returnToLobbyFab = Fab("fab-right-1", Images.iconClose, onClick := (() => Main.gotoLobby()))

    gameState.game.you match {
      case None if gameState.game.cycle == 0 =>
        div(id := "robo-race",
          className := "game",
          returnToLobbyFab,
          replayFab,
          RenderGame(gameState.game, Some { (p, _) =>
            gameState.game.scenario.initialRobots.find(r => r.position == p && !gameState.game.robots.contains(r))
              .foreach(r => SendCommand(gameState, RegisterForGame(r.index))
                .foreach(rerender))
          }),
          div(className := "text-panel", "tap a start position to join the game")
        )

      case None =>
        div(id := "robo-race",
          className := "game",
          returnToLobbyFab,
          replayFab,
          RenderGame(gameState.game, None),
          div(className := "text-panel", "observer mode")
        )

      case Some(you) if you.finished.exists(_.rageQuitted == true) =>
        div(id := "robo-race",
          className := "game",
          returnToLobbyFab,
          replayFab,
          RenderGame(gameState.game, None),
          div(className := "text-panel", "game quitted")
        )

      case Some(you) if you.finished.exists(_.rageQuitted == false) =>
        div(id := "robo-race",
          className := "game",
          returnToLobbyFab,
          replayFab,
          RenderGame(gameState.game, None),
          div(className := "text-panel", "target reached as " + you.finished.get.rank)
        )

      case Some(you) =>
        div(id := "robo-race",
          className := "game",
          Fab("fab-right-1", Images.iconClose, onClick := (() => SendCommand(gameState, DeregisterForGame).foreach(rerender))),
          replayFab,
          RenderGame(gameState.game, None),
          renderInstructionBar(gameState, rerender, you)
        )
    }
  }

  def renderInstructionBar(state: GameState, rerender: GameState => Unit, player: Player): VNode = {
    def instructionSlot(index: Int): VNode = {
      val instruction = state.slots.get(index)
      val focused = state.focusedSlot == index

      val setFocus = onClick := (_ => rerender(state.copy(focusedSlot = index)))
      val resetSlot = onDblClick := (_ => UnsetInstruction(index).apply(state).foreach(rerender))

      instruction match {
        case Some(instr) =>
          span(className := "slot filled" + (if (focused) " focused" else ""),
            setFocus,
            resetSlot,
            img(src := Images.action(instr)),
          )
        case None        =>
          span(className := "slot" + (if (focused) " focused" else ""),
            setFocus,
            resetSlot,
            (index + 1).toString
          )
      }
    }

    def instructionCard(instruction: Instruction): Option[VNode] = {
      val allowed = player.instructionOptions.find(_.instruction == instruction).fold(0)(_.count)
      val used = state.slots.values.count(_ == instruction)
      val free = allowed - used

      if (free > 0) {
        Some(
          div(className := s"action stacked-action-${free.min(5)}",
            img(src := Images.action(instruction)),
            if (free != 1) div(className := "badge", free.toString)
            else None,
            onClick := (_ => PlaceInstruction(instruction, state.focusedSlot).apply(state).foreach(rerender))
          )
        )
      } else {
        None
      }
    }

    div(className := "footer-group",
      div(className := "slots-panel",
        seq((0 until Constants.instructionsPerCycle).map(instructionSlot))),
      div(className := "cards-panel",
        seq(Instruction.instructions.flatMap(instructionCard)))
    )
  }

}
