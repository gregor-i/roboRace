package roborace.frontend.pages
package multiplayer.game

import entities._
import logic.command.{DeregisterForGame, RegisterForGame}
import org.scalajs.dom
import roborace.frontend.pages.components._
import roborace.frontend.pages.components.gameBoard.{Animation, RenderGame}
import roborace.frontend.pages.multiplayer.game.GamePage.Context
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.service.Actions
import roborace.frontend.util.{SnabbdomEventListener, Untyped}
import snabbdom.{Node, Snabbdom}
object GameUi {
  def apply(implicit context: Context): Node = {
    context.local.game.you match {
      case None if context.local.game.cycle == 0 =>
        Body
          .game()
          .child(returnToLobbyFab())
          .child(replayFab())
          .child(
            RenderGame(
              context.local.game,
              Some { (p, _) =>
                context.local.game.scenario.initialRobots
                  .find(r => r.position == p && !context.local.game.robots.contains(r))
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
          .child(returnToLobbyFab())
          .child(replayFab())
          .child(RenderGame(context.local.game, None))
          .child(Node("div.text-panel").text("observer mode"))

      case Some(you: QuittedPlayer) =>
        Body
          .game()
          .children(
            returnToLobbyFab(),
            replayFab(),
            RenderGame(context.local.game, None),
            Node("div.text-panel").text("game quitted")
          )

      case Some(you: FinishedPlayer) =>
        Body
          .game()
          .children(
            returnToLobbyFab(),
            replayFab(),
            RenderGame(context.local.game, None),
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
              .event("click", SnabbdomEventListener.sideeffect(() => Actions.sendCommand(DeregisterForGame))),
            replayFab(),
            RenderGame(context.local.game, None),
            instructionSlots(you),
            cardsBar(you)
          )
    }
  }

  private def replayFab()(implicit context: Context) =
    Fab(Icons.replay)
      .classes("fab-left-1")
      .event(
        "click",
        Snabbdom.event { _ =>
          Untyped(dom.document.querySelector(".game-board svg")).setCurrentTime(
            if (context.local.game.cycle == 0)
              0
            else
              Animation.eventSequenceDuration(context.local.game.events.takeWhile {
                case s: StartCycleEvaluation => s.cycle != context.local.game.cycle - 1
                case _                       => true
              })
          )
        }
      )
      .event("dblclick", SnabbdomEventListener.sideeffect(() => Untyped(dom.document.querySelector(".game-board svg")).setCurrentTime(0)))

  private def returnToLobbyFab()(implicit context: Context) =
    Fab(Icons.close).classes("fab-right-1").event("click", SnabbdomEventListener.set(LobbyPage.load()))

  def instructionSlots(player: RunningPlayer)(implicit context: Context) = {
    def instructionSlot(index: Int): Node = {
      val instruction = context.local.slots.get(index)
      val focused     = context.local.focusedSlot == index

      val setFocus  = SnabbdomEventListener.modify(GameState.focusedSlot.set(index))
      val resetSlot = SnabbdomEventListener.sideeffect(() => Actions.unsetInstruction(index))

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

  private def cardsBar(player: RunningPlayer)(implicit context: Context): Node = {
    def instructionCard(instruction: Instruction): Option[Node] = {
      val allowed = player.instructionOptions.find(_.instruction == instruction).fold(0)(_.count)
      val used    = context.local.slots.values.count(_ == instruction)
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
            .event("click", SnabbdomEventListener.sideeffect(() => Actions.placeInstruction(instruction, context.local.focusedSlot)))
        )
      } else {
        None
      }
    }

    Node("div.nowrap-panel").child(Instruction.instructions.flatMap(instructionCard))
  }

}
