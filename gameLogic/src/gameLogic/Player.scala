package gameLogic

import monocle.macros.Lenses

sealed trait Player {
  val index: Int
  val name: String
}

@Lenses
case class RunningPlayer(index: Int,
                         name: String,
                         robot: Robot,
                         actions: Seq[Action],
                         finished: Option[FinishedStatistic],
                         possibleActions: Seq[Action]) extends Player

@Lenses
case class StartingPlayer(index: Int,
                          name: String,
                          ready: Boolean) extends Player

@Lenses
case class FinishedStatistic(rank: Int, cycle: Int)