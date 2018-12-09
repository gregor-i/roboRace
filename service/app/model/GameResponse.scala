package model

import gameLogic._
import repo.{GameRow, Session}

case class GameResponse(id: String,
                        cycle: Int,
                        scenario: Scenario,
                        robots: Seq[Robot],
                        events: Seq[EventLog],
                        you: Option[Player])

object GameResponse {
  def apply(game: Game, gameId: String)(implicit session: Session): GameResponse = GameResponse(
    id = gameId,
    cycle = game.cycle,
    scenario = game.scenario,
    robots = game.players.map(_.robot),
    events = game.events,
    you = game.players.find(_.name == session.playerId)
  )

  def apply(gameRow: GameRow)(implicit session: Session): Option[GameResponse] =
    gameRow.game.map(game => apply(game, gameRow.id))
}
