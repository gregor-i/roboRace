package gameLogic

case class Player(index: Int,
                  name: String,
                  robot: Robot,
                  actions: Seq[Action],
                  finished: Option[FinishedStatistic])

case class FinishedStatistic(rank: Int, cycle: Int)