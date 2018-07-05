package repo

import anorm._
import gameLogic.Game
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.db.Database

case class GameRow(id: String, owner: String, game: Option[Game])

object GameRow {
  implicit val ordering: Ordering[GameRow] = Ordering.String.on(_.id)
}

@Singleton
class GameRepository @Inject()(db: Database) {
  private val rowParser: RowParser[GameRow] = for {
    id <- SqlParser.str("id")
    owner <- SqlParser.str("owner")
    maybeGame <- SqlParser.str("game").map(data => decode[Game](data).toOption)
  } yield GameRow(id, owner, maybeGame)

  def get(id: String): Option[GameRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM games
            WHERE id = $id"""
        .as(rowParser.singleOpt)
    }

  def list(): Seq[GameRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM games
            ORDER BY id"""
        .as(rowParser.*)
    }

  def save(row: GameRow): Int =
    db.withConnection { implicit con =>
      val data = row.game.map(_.asJson.noSpaces)
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
