package roboRace
package ai

import gameLogic.{Player, Robot, Scenario}


trait Bot {
  def apply(scenario: Scenario, otherRobots: Seq[Robot], player: Player): Seq[Int]
}
