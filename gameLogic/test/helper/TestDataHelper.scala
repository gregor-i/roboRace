package helper

import gameLogic._
import gameLogic.command.{CommandAccepted, CreateGame}
import org.scalatest.Matchers

trait TestDataHelper { _: UpdateChainHelper with Matchers =>
  val p0 = "p0"
  val p1 = "p1"
  val p2 = "p2"

  def createGame(scenario: Scenario = Scenario.default, index: Int = 0)(player: String): Game = {
    val resp = CreateGame(scenario, index)(player)
    resp shouldBe a[CommandAccepted]
    resp.asInstanceOf[CommandAccepted].newState
  }

  def addWall(wall: Wall): CE =
    Game.scenario.modify(s => s.copy(walls = s.walls :+ wall))

  def addPit(pit: Position): CE =
    Game.scenario.modify(s => s.copy(pits = s.pits :+ pit))

  def addTrap(trap: Trap): CE =
    Game.scenario.modify(s => s.copy(traps = s.traps :+ trap))

  def forcedInstructions(player: String)(instructions: Instruction*): CE =
    Game.player(player).modify(_.copy(
      instructionOptions = (instructions ++ Seq.fill(Constants.instructionsPerCycle)(Sleep)).take(Constants.instructionsPerCycle),
      instructionSlots = (0 until Constants.instructionsPerCycle).map(Some.apply)))

  def placeRobot(player: String, robot: Robot): CE =
    (Game.player(player) composeLens Player.robot).set(robot)

  def clearHistory: CE = Game.events.set(Seq.empty)
}
