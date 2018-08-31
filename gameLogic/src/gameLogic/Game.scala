package gameLogic

import monocle.Optional
import monocle.macros.Lenses
import util.OptionalWhere

@Lenses
case class Game(cycle: Int,
                scenario: Scenario,
                players: List[Player],
                events: Seq[EventLog]){
  def log(newEvent: EventLog): Game = copy(events = events :+ newEvent)
}

object Game {
  def player(name: String): Optional[Game, Player] = players.composeOptional(OptionalWhere.where(_.name == name))

  def isFinished(game: Game) : Boolean = game.players.forall(_.finished.isDefined)
}
