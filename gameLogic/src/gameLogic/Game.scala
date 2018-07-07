package gameLogic

import monocle.Optional
import monocle.macros.Lenses
import util.OptionalWhere

@Lenses
case class Game(cycle: Int,
                scenario: Scenario,
                players: List[Player],
                events: Seq[Seq[EventLog]]){
  def addLogs(newEvents: EventLog*): Game = if(events.length > cycle)
    copy(events = events.updated(cycle, events(cycle) ++ newEvents))
  else
    copy(events = events :+ newEvents)
}

object Game {
  def player(name: String): Optional[Game, Player] = players.composeOptional(OptionalWhere.where(_.name == name))

  def isFinished(game: Game) : Boolean = game.players.forall(_.finished.isDefined)
}
