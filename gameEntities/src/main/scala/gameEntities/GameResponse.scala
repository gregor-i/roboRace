package gameEntities

case class GameResponse(id: String, cycle: Int, scenario: Scenario, robots: Seq[Robot], events: Seq[EventLog], you: Option[Player])
