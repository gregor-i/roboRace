package helper

import gameEntities._
import gameLogic._
import gameLogic.command.CreateGame
import org.scalatest.matchers.should.Matchers

trait TestDataHelper {
  _: UpdateChainHelper with Matchers =>
  val p0 = "p0"
  val p1 = "p1"
  val p2 = "p2"

  val validInstructionSequence = Seq(MoveForward, TurnRight, MoveForward, TurnLeft, MoveForward)

  def createGame(scenario: Scenario = DefaultScenario.default, index: Int = 0)(player: String): Game = {
    CreateGame(scenario, index)(player) match {
      case CommandRejected(reason)   => fail(s"command was rejected with $reason")
      case CommandAccepted(newState) => newState
    }
  }

  def addWall(wall: Wall): CE =
    Game.scenario.modify(s => s.copy(walls = s.walls :+ wall))

  def addPit(pit: Position): CE =
    Game.scenario.modify(s => s.copy(pits = s.pits :+ pit))

  def addTrap(trap: Trap): CE =
    Game.scenario.modify(s => s.copy(traps = s.traps :+ trap))

  def forcedInstructions(player: String)(instructions: Instruction*): CE = {
    val filledInstrs = (instructions ++ Seq.fill(Constants.instructionsPerCycle)(Sleep))
      .take(Constants.instructionsPerCycle)
    Lenses
      .runningPlayer(player)
      .modify(
        _.copy(
          instructionOptions = filledInstrs.groupBy(identity).map(t => InstructionOption(t._1, t._2.size)).toSeq,
          instructionSlots = filledInstrs
        )
      )
  }

  def forceRobot(id: String, robot: Robot): CE =
    Lenses.robot(id).set(robot)

  def clearHistory: CE = Game.events.set(Seq.empty)
}
