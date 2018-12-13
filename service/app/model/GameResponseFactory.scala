package model

import gameEntities.{Game, GameResponse}
import repo.{GameRow, Session}



object GameResponseFactory {
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
