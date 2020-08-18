package entities

import monocle.macros.Lenses

@Lenses
case class Game(cycle: Int, scenario: Scenario, players: List[Player], events: Seq[EventLog]) {
  def log(event: EventLog): Game =
    this.copy(events = events :+ event)
}
