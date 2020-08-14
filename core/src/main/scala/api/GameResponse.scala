package api

import entities.{EventLog, Player, Robot, Scenario}

case class GameResponse(
    id: String,
    cycle: Int,
    scenario: Scenario,
    robots: Seq[Robot],
    events: Seq[EventLog],
    you: Option[Player],
    ownedByYou: Boolean
)
