package gameLogic

sealed trait GameState {
  def fold[A](ifNotDefined: GameNotDefined.type => A)
             (ifNotStarted: GameNotStarted => A)
             (ifRunning: GameRunning => A)
             (ifFinished: GameFinished => A): A = this match {
    case g: GameNotDefined.type => ifNotDefined(g)
    case g: GameNotStarted => ifNotStarted(g)
    case g: GameRunning => ifRunning(g)
    case g: GameFinished => ifFinished(g)
  }

  def stateDescription: String = getClass.getSimpleName.filter(_ != '$')
}

case object GameNotDefined extends GameState

case class GameNotStarted(scenario: GameScenario,
                          playerNames: Seq[String]) extends GameState

case class GameRunning(cycle: Int,
                       scenario: GameScenario,
                       players: Seq[Player]) extends GameState

case class GameFinished(players: Seq[Player],
                        scenario: GameScenario) extends GameState