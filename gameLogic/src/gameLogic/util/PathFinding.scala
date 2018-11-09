package gameLogic.util

import gameLogic.gameUpdate.{MoveRobots, ScenarioEffects}
import gameLogic.{Direction, Position, Scenario}

import scala.annotation.tailrec

object PathFinding {
  def to(target: Position)(scenario: Scenario): Map[Position, List[Direction]] ={
    @tailrec
    def loop(currentLevel: Set[Position], visited: Set[Position], accumulator: Map[Position, List[Direction]]): Map[Position, List[Direction]] =
      if (currentLevel.isEmpty)
        accumulator
      else {
        val nextLevel = (for {
          from <- currentLevel
          direction <- Direction.directions
          to = direction(from)
          if !visited(to)
          if !ScenarioEffects.isPit(scenario, to)
          if !MoveRobots.blockedByWall(scenario)(from, direction)
        } yield (to, direction.back :: accumulator(from))).toMap
        loop(nextLevel.keySet, visited ++ nextLevel.keySet, accumulator ++ nextLevel)
      }

    if(ScenarioEffects.isPit(scenario, target))
      Map.empty
    else
      loop(
        currentLevel = Set(target),
        visited = Set(target),
        accumulator = Map(target -> List.empty))
  }

  def toTarget(scenario: Scenario): Map[Position, List[Direction]] = to(scenario.targetPosition)(scenario)
}