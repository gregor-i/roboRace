package gameEntities

case class Game(cycle: Int, scenario: Scenario, players: List[Player], events: Seq[EventLog])
