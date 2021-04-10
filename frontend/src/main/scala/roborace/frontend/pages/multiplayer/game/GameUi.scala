package roborace.frontend.pages
package multiplayer.game

import scala.concurrent.ExecutionContext.Implicits.global

import entities._
import logic.command.{DeregisterForGame, RegisterForGame}
import org.scalajs.dom
import roborace.frontend.pages.components._
import roborace.frontend.pages.components.gameBoard.{Animation, RenderGame}
import roborace.frontend.pages.multiplayer.game.GamePage.Context
import roborace.frontend.pages.multiplayer.lobby.LobbyPage
import roborace.frontend.service.{Actions, Service, ServiceTrait}
import roborace.frontend.util.{SnabbdomEventListener, Untyped}
import snabbdom.{Event, Node, Snabbdom}
object GameUi extends snabbdom.Syntax {
  def apply(implicit context: Context): Node = {
    context.local.game.entity.players.find(_.id == context.global.sessionId) match {
      case None if context.local.game.entity.cycle == 0 =>
        Body
          .game()
          .child(returnToLobbyFab())
          .child(replayFab())
          .child(openPlayerListFab())
          .child(syncFab())
          .child(
            RenderGame(
              context.local.game,
              Some { (p, _) =>
                context.local.game.entity.scenario.initialRobots
                  .find(r => r.position == p && !context.local.game.entity.players.contains(r))
                  .foreach(r => Actions.sendCommand(RegisterForGame(r.index)))
              }
            )
          )
          .child(
            Node("div").classes("text-panel").text("tap a start position to join the game")
          )
          .childOptional(playerListModal())

      case None =>
        Body
          .game()
          .child(returnToLobbyFab())
          .child(replayFab())
          .child(openPlayerListFab())
          .child(syncFab())
          .child(RenderGame(context.local.game, None))
          .child(Node("div.text-panel").text("observer mode"))
          .childOptional(playerListModal())

      case Some(you: QuitedPlayer) =>
        Body
          .game()
          .children(
            returnToLobbyFab(),
            replayFab(),
            openPlayerListFab(),
            syncFab(),
            RenderGame(context.local.game, None),
            Node("div.text-panel").text("game quitted")
          )
          .childOptional(playerListModal())

      case Some(you: FinishedPlayer) =>
        Body
          .game()
          .children(
            returnToLobbyFab(),
            replayFab(),
            openPlayerListFab(),
            syncFab(),
            RenderGame(context.local.game, None),
            Node("div.text-panel").text("target reached as " + you.rank)
          )
          .childOptional(playerListModal())

      case Some(you: RunningPlayer) =>
        val color = RobotColor.dark(you.index)
        Body
          .game()
          .style("--highlight-color", color)
          .children(
            Fab(Icons.close)
              .classes("fab-right-1")
              .event("click", SnabbdomEventListener.sideeffect(() => Actions.sendCommand(DeregisterForGame))),
            openPlayerListFab(),
            syncFab(),
            replayFab(),
            RenderGame(context.local.game, None),
            instructionSlots(you),
            cardsBar(you)
          )
          .childOptional(playerListModal())
    }
  }

  private def replayFab()(implicit context: Context) =
    Fab(Icons.replay)
      .classes("fab-left-1")
      .event(
        "click",
        (_: Event) => {
          Untyped(dom.document.querySelector(".game-board svg")).setCurrentTime(
            if (context.local.game.entity.cycle == 0)
              0
            else
              Animation.eventSequenceDuration(context.local.game.entity.events.takeWhile {
                case s: StartCycleEvaluation => s.cycle != context.local.game.entity.cycle - 1
                case _                       => true
              })
          )
        }
      )
      .event("dblclick", SnabbdomEventListener.sideeffect(() => Untyped(dom.document.querySelector(".game-board svg")).setCurrentTime(0)))

  private def syncFab()(implicit context: Context): Node =
    Fab(Icons.sync)
      .classes("fab-right-2")
      .event[Event](
        "click",
        _ =>
          Service
            .getGame(context.local.game.id)
            .map(GameState.game.set)
            .foreach(f => context.update(f(context.local)))
      )

  private def openPlayerListFab()(implicit context: Context) =
    Fab(Icons.list)
      .classes("fab-left-2")
      .event("click", SnabbdomEventListener.modify(GameState.playerModalOpened.set(true)))

  private def returnToLobbyFab()(implicit context: Context) =
    Fab(Icons.close).classes("fab-right-1").event("click", SnabbdomEventListener.set(LobbyPage.load()))

  def playerListModal()(implicit context: Context): Option[Node] =
    if (context.local.playerModalOpened) {
      Some(
        Modal(closeAction = SnabbdomEventListener.modify(GameState.playerModalOpened.set(false)))(
          Node("h1.title").text("Player List"),
          Node("table.table.is-fullwidth")
            .child(
              "tr"
                .child("th".text("Player"))
                .child("th".text("State"))
            )
            .child(
              context.local.game.entity.players.map(
                player =>
                  "tr"
                    .child("td".child("img.image.is-64x64".attr("src", Images.player(player.index))))
                    .child(
                      "td".text(
                        player match {
                          case player: RunningPlayer if player.instructionSlots.isEmpty => "waiting for"
                          case _: RunningPlayer                                         => "ready"
                          case _: QuitedPlayer                                          => "left the game"
                          case player: FinishedPlayer                                   => s"finished as ${player.rank}"
                        }
                      )
                    )
              )
            )
        )
      )
    } else {
      None
    }

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
