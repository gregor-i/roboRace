package roborace.frontend.pages.game

import entities._
import org.scalajs.dom
import roborace.frontend.FrontendState
import roborace.frontend.pages.components.gameBoard.{Animation, RenderGame}
import roborace.frontend.pages.components.{Body, Fab, Icons, Images, RobotColor}
import roborace.frontend.pages.lobby.LobbyPage
import roborace.frontend.service.Actions
import roborace.frontend.util.Untyped
import snabbdom.{Node, Snabbdom}

import scala.concurrent.ExecutionContext.Implicits.global

object GameUi {
  def apply(implicit state: GameState, update: FrontendState => Unit): Node = {
    state.game.you match {
      case None if state.game.cycle == 0 =>
        Body
          .game()
          .child(returnToLobbyFab(state, update))
          .child(replayFab(state, update))
          .child(
            RenderGame(
              state.game,
              Some { (p, _) =>
                state.game.scenario.initialRobots
                  .find(r => r.position == p && !state.game.robots.contains(r))
                  .foreach(r => Actions.sendCommand(RegisterForGame(r.index)))
              }
            )
          )
          .child(
            Node("div").classes("text-panel").text("tap a start position to join the game")
          )

      case None =>
        Body
          .game()
          .child(returnToLobbyFab(state, update))
          .child(replayFab(state, update))
          .child(RenderGame(state.game, None))
          .child(Node("div.text-panel").text("observer mode"))

      case Some(you: QuittedPlayer) =>
        Body
          .game()
          .children(
            returnToLobbyFab(state, update),
            replayFab(state, update),
            RenderGame(state.game, None),
            Node("div.text-panel").text("game quitted")
          )

      case Some(you: FinishedPlayer) =>
        Body
          .game()
          .children(
            returnToLobbyFab(state, update),
            replayFab(state, update),
            RenderGame(state.game, None),
            Node("div.text-panel").text("target reached as " + you.rank)
          )

      case Some(you: RunningPlayer) =>
        val color = RobotColor.dark(you.index)
        Body
          .game()
          .style("--highlight-color", color)
          .children(
            Fab(Icons.close)
              .classes("fab-right-1")
              .event("click", Snabbdom.event(_ => Actions.sendCommand(DeregisterForGame))),
            replayFab(state, update),
            RenderGame(state.game, None),
            instructionSlots(you),
            cardsBar(you)
          )
    }
  }

  private def replayFab(state: GameState, update: FrontendState => Unit) =
    Fab(Icons.replay)
      .classes("fab-left-1")
      .event(
        "click",
        Snabbdom.event { _ =>
          Untyped(dom.document.querySelector(".game-board svg")).setCurrentTime(
            if (state.game.cycle == 0)
              0
            else
              Animation.eventSequenceDuration(state.game.events.takeWhile {
                case s: StartCycleEvaluation => s.cycle != state.game.cycle - 1
                case _                       => true
              })
          )
        }
      )
      .event("dblclick", Snabbdom.event(_ => Untyped(dom.document.querySelector(".game-board svg")).setCurrentTime(0)))

  private def returnToLobbyFab(implicit state: GameState, update: FrontendState => Unit) =
    Fab(Icons.close).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load())))

  def instructionSlots(player: RunningPlayer)(implicit state: GameState, update: FrontendState => Unit) = {
    def instructionSlot(index: Int): Node = {
      val instruction = state.slots.get(index)
      val focused     = state.focusedSlot == index

      val setFocus  = Snabbdom.event(_ => update(state.copy(focusedSlot = index)))
      val resetSlot = Snabbdom.event(_ => Actions.unsetInstruction(index))

      instruction match {
        case Some(instr) =>
          Node("span.slot.filled")
            .`class`("focused", focused)
            .event("click", setFocus)
            .event("dblclick", resetSlot)
            .child(Node("img").attr("src", Images.instructionIcon(instr)))
        case None =>
          Node("span.slot").`class`("focused", focused).event("click", setFocus).text((index + 1).toString)
      }
    }

    Node("div.nowrap-panel").child((0 until Constants.instructionsPerCycle).map(instructionSlot))
  }

  private def cardsBar(player: RunningPlayer)(implicit state: GameState, update: FrontendState => Unit): Node = {
    def instructionCard(instruction: Instruction): Option[Node] = {
      val allowed = player.instructionOptions.find(_.instruction == instruction).fold(0)(_.count)
      val used    = state.slots.values.count(_ == instruction)
      val free    = allowed - used

      if (free > 0) {
        Some(
          Node("div.instruction-card")
            .classes(s"stacked-instruction-card-${free.min(5)}")
            .child(
              Node("img")
                .attr("src", Images.instructionIcon(instruction))
            )
            .childOptional(
              if (free != 1) Some(Node("div.badge").text(free.toString))
              else None
            )
            .event("click", Snabbdom.event(_ => Actions.placeInstruction(instruction, state.focusedSlot)))
        )
      } else {
        None
      }
    }

    Node("div.nowrap-panel").child(Instruction.instructions.flatMap(instructionCard))
  }

}
