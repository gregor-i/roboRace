package roboRace
package ai

import gameLogic.{Instruction, Robot, Scenario}


trait Bot {
  def apply(scenario: Scenario, otherRobots: Seq[Robot])
           (thisRobot: Robot, instructionOptions: Seq[Instruction]): Seq[Int]
}
