package roborace.frontend.game

import gameEntities._
import org.scalajs.dom
import roborace.frontend.FrontendState
import roborace.frontend.components.{Body, Fab, Icon, Images}
import roborace.frontend.components.gameBoard.{Animation, RenderGame}
import roborace.frontend.lobby.LobbyPage
import roborace.frontend.util.Untyped
import snabbdom.{Node, Snabbdom}

import scala.concurrent.ExecutionContext.Implicits.global

object GameUi {
  def apply(state: GameState, update: FrontendState => Unit): Node = {
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
                  .foreach(
                    r =>
                      SendCommand(state, RegisterForGame(r.index))
                        .foreach(update)
                  )
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
        Body
          .game()
          .children(
            Fab(Icon.close)
              .classes("fab-right-1")
              .event("click", Snabbdom.event(_ => SendCommand(state, DeregisterForGame).foreach(update))),
            replayFab(state, update),
            RenderGame(state.game, None),
            instructionBar(state, update, you)
          )
    }
  }

  private def replayFab(state: GameState, update: FrontendState => Unit) =
    Fab(Icon.replay)
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

  private def returnToLobbyFab(state: GameState, update: FrontendState => Unit) =
    Fab(Icon.close).classes("fab-right-1").event("click", Snabbdom.event(_ => update(LobbyPage.load())))

  private def instructionBar(state: GameState, update: FrontendState => Unit, player: RunningPlayer): Node = {
    def instructionSlot(index: Int): Node = {
      val instruction = state.slots.get(index)
      val focused     = state.focusedSlot == index

      val setFocus  = Snabbdom.event(_ => update(state.copy(focusedSlot = index)))
      val resetSlot = Snabbdom.event(_ => UnsetInstruction(index).apply(state).foreach(update))

      instruction match {
        case Some(instr) =>
          Node("span.slot.filled")
            .`class`("focused", focused)
            .event("click", setFocus)
            .event("dblClick", resetSlot)
            .child(Node("img").attr("src", Images.action(instr)))
        case None =>
          Node("span.slot").`class`("focused", focused).event("click", setFocus).event("dblClick", resetSlot).text((index + 1).toString)
      }
    }

    def instructionCard(instruction: Instruction): Option[Node] = {
      val allowed = player.instructionOptions.find(_.instruction == instruction).fold(0)(_.count)
      val used    = state.slots.values.count(_ == instruction)
      val free    = allowed - used

      if (free > 0) {
        Some(
          Node("div.action")
            .classes(s"stacked-action-${free.min(5)}")
            .child(
              Node("img")
                .attr("src", Images.action(instruction))
            )
            .childOptional(
              if (free != 1) Some(Node("div.badge").text(free.toString))
              else None
            )
            .event("click", Snabbdom.event(_ => PlaceInstruction(instruction, state.focusedSlot).apply(state).foreach(update)))
        )
      } else {
        None
      }
    }

    Node("div.footer-group")
      .child(Node("div.slots-panel").child((0 until Constants.instructionsPerCycle).map(instructionSlot)))
      .child(Node("div.cards-panel").child(Instruction.instructions.flatMap(instructionCard)))
  }

}