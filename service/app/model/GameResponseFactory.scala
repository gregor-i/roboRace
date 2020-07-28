package model

import gameEntities.{Game, GameResponse}
import gameLogic.{Lenses, PlayerLenses}
import repo.{GameRow, Session}

object GameResponseFactory {
  def apply(game: Game, gameId: String)(implicit session: Session): GameResponse = GameResponse(
    id = gameId,
    cycle = game.cycle,
    scenario = game.scenario,
    robots = Lenses.runningPlayers.composeLens(PlayerLenses.robot).getAll(game),
    events = game.events,
    you = game.players.find(_.id == session.playerId)
  )

  def apply(gameRow: GameRow)(implicit session: Session): Option[GameResponse] =
    gameRow.game.map(game => apply(game, gameRow.id))
}
