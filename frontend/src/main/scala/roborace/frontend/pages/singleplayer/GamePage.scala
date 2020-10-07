package roborace.frontend.pages
package singleplayer

import entities._
import logic.command.{Command, CreateGame}
import logic.gameUpdate.Cycle
import monocle.Lens
import monocle.macros.Lenses
import roborace.frontend.pages.components.gameBoard.RenderGame
import roborace.frontend.pages.components.{Body, Images, RobotColor}
import roborace.frontend.util.SnabbdomEventListener
import roborace.frontend.{GlobalState, PageState}
import snabbdom.{Node, Snabbdom}

@Lenses
case class GameState(game: Game, focusedSlot: Int, instructionSlots: Seq[Option[Instruction]]) extends PageState {

  def setInstruction(slot: Int, instruction: Instruction): GameState = {
    val newSlots = instructionSlots.updated(slot, Some(instruction))

    Command
      .setInstructions(newSlots.flatten)("singleplayer")(game)
      .map(Cycle.apply) match {
      case Right(game) =>
        this.copy(game = game, focusedSlot = 0, instructionSlots = Seq.fill(Constants.instructionsPerCycle)(None))
      case Left(_) =>
        this.copy(game = game, focusedSlot = (focusedSlot + 1) % Constants.instructionsPerCycle, instructionSlots = newSlots)
    }
  }

  def resetInstruction(slot: Int): GameState = {
    val newSlots = instructionSlots.updated(slot, None)
    GameState.instructionSlots.set(newSlots)(this)
  }

}

object GameState {
  def start(scenario: Scenario): GameState = {
    val game = CreateGame(scenario, 0)("singleplayer").getOrElse(throw new Exception())

    new GameState(
      game = game,
      focusedSlot = 0,
      instructionSlots = Seq.fill(Constants.instructionsPerCycle)(None)
    )
  }

  val playerLens: Lens[GameState, RunningPlayer] =
    Lens[GameState, RunningPlayer](
      get = state => state.game.players.find(_.id == "singleplayer").get.asInstanceOf[RunningPlayer]
    )(
      set = newPlayer =>
        state =>
          state.copy(game = state.game.copy(players = state.game.players.map {
            case thePlayer if thePlayer.id == "singleplayer" => newPlayer
            case otherPlayer                                 => otherPlayer
          }))
    )

  val placedInstructionSlots =
    playerLens.composeLens(RunningPlayer.instructionSlots)
}

object GamePage extends Page[GameState] {
  override def stateFromUrl: PartialFunction[(GlobalState, Path, QueryParameter), PageState] = {
    case (_, s"/singleplayer/${id}", _) if Levels.map.contains(id) =>
      val level = Levels.map(id)
      GameState.start(level)
  }

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] =
    Some(s"/singleplayer/${Levels.id(state.game.scenario)}" -> Map.empty)

  override def render(implicit context: Context): Node = {
    context.local.game.players.headOption match {
      case Some(you: FinishedPlayer) =>
        Body
          .game()
          .children(
//            returnToLobbyFab(context.local, update),
//            replayFab(context.local, update),
            RenderGame(context.local.game, None).event("click", SnabbdomEventListener.set(SelectLevelState())),
            Node("div.text-panel").text(s"Level finished after ${context.local.game.cycle} Turns!")
          )

      case Some(you: RunningPlayer) =>
        val color = RobotColor.dark(you.index)
        Body
          .game()
          .style("--highlight-color", color)
          .children(
//            Fab(Icons.close)
//              .classes("fab-right-1")
//              .event("click", Snabbdom.event(_ => Actions.sendCommand(DeregisterForGame))),
//            replayFab(state, update),
            RenderGame(context.local.game, None),
            instructionSlots(you),
            cardsBar(you)
          )

      case None | Some(_: QuittedPlayer) =>
        context.update(ErrorState("This should not have happened"))
        Node("div")
    }
  }

  // todo: make generic and unify with GamePage
  private def instructionSlots(player: RunningPlayer)(implicit context: Context): Node = {
    def instructionSlot(index: Int): Node = {
      val instruction = context.local.instructionSlots(index)
      val focused     = context.local.focusedSlot == index

      val setFocus  = SnabbdomEventListener.modify(GameState.focusedSlot.set(index))
      val resetSlot = SnabbdomEventListener.modify[State](_.resetInstruction(index))

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

  // todo: make generic and unify with GamePage
  private def cardsBar(player: RunningPlayer)(implicit context: Context): Node = {
    def instructionCard(instruction: Instruction): Option[Node] = {
      val allowed = player.instructionOptions.find(_.instruction == instruction).fold(0)(_.count)
      val used    = context.local.instructionSlots.count(_.contains(instruction))
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
            .event(
              "click",
              Snabbdom.event(
                _ => {
                  val newLocal = context.local.setInstruction(context.local.focusedSlot, instruction)
                  val newGlobal =
                    if (newLocal.game.players.exists(p => p.id == "singleplayer" && p.isInstanceOf[FinishedPlayer])) {
                      context.global.copy(
                        finishedSinglePlayerLevels = context.global.finishedSinglePlayerLevels + context.local.game.scenario.hashCode.toHexString
                      )
                    } else context.global

                  context.update(newGlobal, newLocal)
                }
              )
            )
        )
      } else {
        None
      }
    }

    Node("div.nowrap-panel").child(Instruction.instructions.flatMap(instructionCard))
  }
}
