package gameLogic

import monocle.Optional
import monocle.macros.Lenses
import util.OptionalWhere

@Lenses
case class Game(cycle: Int,
                scenario: Scenario,
                players: List[RunningPlayer],
                events: Seq[EventLog]){
  def addLogs(events: EventLog*): Game = copy(events = this.events ++ events)
}

object Game {
  def player(name: String): Optional[Game, RunningPlayer] = players.composeOptional(OptionalWhere.where(_.name == name))

  def isFinished(game: Game) : Boolean = game.players.forall(_.finished.isDefined)
}
