package gameLogic

sealed trait GameState {
  def fold[A](ifInitial: InitialGame.type => A)
             (ifStarting: GameStarting => A)
             (ifRunning: GameRunning => A)
             (ifFinished: GameFinished => A): A = this match {
    case g: InitialGame.type => ifInitial(g)
    case g: GameStarting => ifStarting(g)
    case g: GameRunning => ifRunning(g)
    case g: GameFinished => ifFinished(g)
  }

  def stateDescription: String = getClass.getSimpleName.filter(_ != '$')
}

case object InitialGame extends GameState

case class GameStarting(scenario: GameScenario,
                        players: Seq[StartingPlayer]) extends GameState

case class GameRunning(cycle: Int,
                       scenario: GameScenario,
                       players: Seq[RunningPlayer]) extends GameState

case class GameFinished(players: Seq[RunningPlayer],
                        scenario: GameScenario) extends GameState