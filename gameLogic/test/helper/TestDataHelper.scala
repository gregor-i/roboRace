package helper

import gameEntities._
import gameLogic._
import gameLogic.command.CreateGame
import gameLogic.gameUpdate.Cycle
import org.scalatest.Matchers

trait TestDataHelper { _: UpdateChainHelper with Matchers =>
  val p0 = "p0"
  val p1 = "p1"
  val p2 = "p2"

  def createGame(scenario: Scenario = DefaultScenario.default, index: Int = 0)(player: String): Game = {
    CreateGame(scenario, index)(player) match {
      case CommandRejected(reason) => fail(s"command was rejected with $reason")
      case CommandAccepted(newState) => newState
    }
  }

  def addWall(wall: Wall): CE =
    Lenses.scenario.modify(s => s.copy(walls = s.walls :+ wall))

  def addPit(pit: Position): CE =
    Lenses.scenario.modify(s => s.copy(pits = s.pits :+ pit))

  def addTrap(trap: Trap): CE =
    Lenses.scenario.modify(s => s.copy(traps = s.traps :+ trap))

  def forcedInstructions(player: String)(instructions: Instruction*): CE =
    Lenses.player(player).modify(_.copy(
      instructionOptions = (instructions ++ Seq.fill(Constants.instructionsPerCycle)(Sleep)).take(Constants.instructionsPerCycle),
      instructionSlots = (0 until Constants.instructionsPerCycle).map(Some.apply)))

  def placeRobot(player: String, robot: Robot): CE =
    Lenses.robot(player).set(robot)

  def clearHistory: CE = Lenses.events.set(Seq.empty)
}
