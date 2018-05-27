package repo

import anorm._
import gameLogic.GameState
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.db.Database

@Singleton
class GameRepository @Inject()(db: Database){
  private val gameParser: RowParser[Option[GameState]] = SqlParser.str("game").map(data => decode[GameState](data).toOption)
  private val rowParser: RowParser[(String, Option[GameState])] = for {
    id <- SqlParser.str("id")
    game <- gameParser
  } yield (id, game)


  def get(id: String): Option[GameState] =
    db.withConnection { implicit con =>
      SQL"""SELECT game
          FROM games
          WHERE id = $id"""
        .as(gameParser.singleOpt).flatten
    }

  def list(): Seq[(String, GameState)] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM games"""
        .as(rowParser.*)
        .collect { case (id, Some(game)) => (id, game) }
    }

  def save(id: String, gameState: GameState): Unit = db.withConnection { implicit con =>
    val data = gameState.asJson.noSpaces
    SQL"""INSERT INTO games (id, game)
          VALUES ($id, $data)
          ON CONFLICT (id) DO UPDATE
          SET game = $data"""
      .executeUpdate()
  }
  def delete(id: String): Unit= db.withConnection{ implicit con =>
    SQL"""DELETE
          FROM games
          WHERE id = $id"""
      .executeUpdate()
  }
}
