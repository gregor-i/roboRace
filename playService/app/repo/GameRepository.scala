package repo

import anorm._
import gameLogic.{Game, Game}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.db.Database

case class GameRow(id: String, owner: String, game: Game)

object GameRow {
  private val orderingState: Ordering[Game] = Ordering.Int.on {
    case _: Game => 1
  }

  implicit val ordering: Ordering[GameRow] = Ordering.Tuple2(orderingState, Ordering.String).on(row => (row.game, row.id))
}

@Singleton
class GameRepository @Inject()(db: Database) {
  private val rowParser: RowParser[Option[GameRow]] = for {
    id <- SqlParser.str("id")
    owner <- SqlParser.str("owner")
    maybeGame <- SqlParser.str("game").map(data => decode[Game](data).toOption)
  } yield maybeGame.map(game => GameRow(id, owner, game))

  def get(id: String): Option[GameRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM games
            WHERE id = $id"""
        .as(rowParser.singleOpt).flatten
    }

  def list(): Seq[GameRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM games
            ORDER BY id"""
        .as(rowParser.*).flatten
    }

  def save(row: GameRow): Int =
    db.withConnection { implicit con =>
      val data = row.game.asJson.noSpaces
      SQL"""INSERT INTO games (id, owner, game)
            VALUES (${row.id}, ${row.owner}, $data)
            ON CONFLICT (id) DO UPDATE
            SET game = $data,
                owner = ${row.owner}"""
        .executeUpdate()
    }

  def delete(id: String): Int =
    db.withConnection { implicit con =>
      SQL"""DELETE
            FROM games
            WHERE id = $id"""
        .executeUpdate()
    }
}
