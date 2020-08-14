package model

import gameEntities.{Game, GameResponse, RunningPlayer}
import gameLogic.{Lenses, PlayerLenses}
import repo.{GameRow, Session}

object GameResponseFactory {

  def apply(gameRow: GameRow)(implicit session: Session): Option[GameResponse] =
    gameRow.game.map(game => apply(gameRow, game)(session))

  def apply(gameRow: GameRow, game: Game)(implicit session: Session): GameResponse =
    GameResponse(
      id = gameRow.id,
      cycle = game.cycle,
      scenario = game.scenario,
      robots = Lenses.runningPlayers.composeLens(RunningPlayer.robot).getAll(game),
      events = game.events,
      you = game.players.find(_.id == session.playerId),
      ownedByYou = gameRow.owner == session.playerId
    )

}
