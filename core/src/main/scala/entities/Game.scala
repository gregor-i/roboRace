package entities

import monocle.macros.Lenses

@Lenses
case class Game(cycle: Int, scenario: Scenario, players: List[Player], events: Seq[EventLog])
