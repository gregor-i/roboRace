package gameLogic

sealed trait Player {
  val index: Int
  val name: String
}

case class RunningPlayer(index: Int,
                         name: String,
                         robot: Robot,
                         actions: Seq[Action],
                         finished: Option[FinishedStatistic],
                         possibleActions: Seq[Action]) extends Player

case class StartingPlayer(index: Int,
                          name: String,
                          ready: Boolean) extends Player

case class FinishedStatistic(rank: Int, cycle: Int)