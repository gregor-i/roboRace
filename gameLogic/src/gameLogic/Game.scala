package gameLogic

import monocle.Traversal
import monocle.function.Each.each
import monocle.macros.Lenses
import monocle.unsafe.UnsafeSelect

@Lenses
case class Game(cycle: Int,
                scenario: Scenario,
                players: List[Player],
                events: Seq[EventLog]){
  def log(newEvent: EventLog): Game = copy(events = events :+ newEvent)
}

object Game {
  def player(name: String): Traversal[Game, Player] = players.composeTraversal(each).composePrism(UnsafeSelect.unsafeSelect[Player](_.name == name))
}
