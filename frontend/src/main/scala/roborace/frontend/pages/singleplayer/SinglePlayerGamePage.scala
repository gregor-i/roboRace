package roborace.frontend.pages
package singleplayer

import api.User
import entities._
import logic.command.{Command, CreateGame}
import logic.gameUpdate.Cycle
import monocle.Lens
import monocle.macros.Lenses
import roborace.frontend.FrontendState
import roborace.frontend.pages.components.gameBoard.RenderGame
import roborace.frontend.pages.components.{Body, Images, RobotColor}
import snabbdom.{Node, Snabbdom}

@Lenses
case class SinglePlayerGameState(game: Game, focusedSlot: Int, instructionSlots: Seq[Option[Instruction]]) extends FrontendState {

  def setInstruction(slot: Int, instruction: Instruction): SinglePlayerGameState = {
    val newSlots = instructionSlots.updated(slot, Some(instruction))

    Command
      .setInstructions(newSlots.flatten)("singleplayer")(game)
      .map(Cycle.apply) match {
      case Right(game) => this.copy(game = game, focusedSlot = 0, instructionSlots = Seq.fill(Constants.instructionsPerCycle)(None))
      case Left(_) =>
        this.copy(game = game, focusedSlot = (focusedSlot + 1) % Constants.instructionsPerCycle, instructionSlots = newSlots)
    }
  }

  def resetInstruction(slot: Int): SinglePlayerGameState = {
    val newSlots = instructionSlots.updated(slot, None)
    SinglePlayerGameState.instructionSlots.set(newSlots)(this)
  }

}

object SinglePlayerGameState {
  def start(scenario: Scenario): SinglePlayerGameState = {
    val game = CreateGame(scenario, 0)("singleplayer").getOrElse(throw new Exception())

    new SinglePlayerGameState(
      game = game,
      focusedSlot = 0,
      instructionSlots = Seq.fill(Constants.instructionsPerCycle)(None)
    )
  }

  val playerLens: Lens[SinglePlayerGameState, RunningPlayer] =
    Lens[SinglePlayerGameState, RunningPlayer](
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

object SinglePlayerGamePage extends Page[SinglePlayerGameState] {
  override def stateFromUrl: PartialFunction[(Option[User], Path, QueryParameter), FrontendState] = PartialFunction.empty

  override def stateToUrl(state: State): Option[(Path, QueryParameter)] = None

  override def render(implicit state: State, update: Update): Node = {
    state.game.players.headOption match {
      case Some(you: FinishedPlayer) =>
        Body
          .game()
          .children(
//            returnToLobbyFab(state, update),
//            replayFab(state, update),
            RenderGame(state.game, None),
            Node("div.text-panel").text(s"Level finished after ${state.game.cycle} Turns!")
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
            RenderGame(state.game, None),
            instructionSlots(you),
            cardsBar(you)
          )

      case None | Some(_: QuittedPlayer) =>
        update(ErrorState("This should not have happened"))
        Node("div")
    }
  }

  // todo: make generic and unify with GamePage
  private def instructionSlots(player: RunningPlayer)(implicit state: State, update: Update): Node = {
    def instructionSlot(index: Int): Node = {
      val instruction = state.instructionSlots(index)
      val focused     = state.focusedSlot == index

      val setFocus  = Snabbdom.event(_ => update(state.copy(focusedSlot = index)))
      val resetSlot = Snabbdom.event(_ => update(state.resetInstruction(index)))

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
  private def cardsBar(player: RunningPlayer)(implicit state: State, update: Update): Node = {
    def instructionCard(instruction: Instruction): Option[Node] = {
      val allowed = player.instructionOptions.find(_.instruction == instruction).fold(0)(_.count)
      val used    = state.instructionSlots.count(_.contains(instruction))
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
                _ => update(state.setInstruction(state.focusedSlot, instruction))
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
