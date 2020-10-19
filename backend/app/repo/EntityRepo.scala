package repo

import anorm.{RowParser, SqlParser, _}
import api.WithId
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import play.api.db.Database

abstract class EntityRepo[E: Decoder: Encoder](tableName: String, rowEntity: String, db: Database) {
  private val rowParser: RowParser[WithId[Option[E]]] = for {
    id     <- SqlParser.str("id")
    owner  <- SqlParser.str("owner")
    maybeE <- SqlParser.str(rowEntity).map(data => decode[E](data).toOption)
  } yield WithId[Option[E]](id, owner, maybeE)

  val rowToEntity: PartialFunction[WithId[Option[E]], WithId[E]] = {
    case withId @ WithId(_, _, Some(entity)) => withId.copy(entity = entity)
  }

  def list(): List[WithId[Option[E]]] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM #${tableName}"""
        .as(rowParser.*)
    }

  def get(id: String): Option[WithId[Option[E]]] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM #${tableName}
            WHERE id = $id"""
        .as(rowParser.singleOpt)
    }

  def listPublic(): List[WithId[Option[E]]] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM #${tableName}
            WHERE published = true"""
        .as(rowParser.*)
    }

  def listByUser(userId: String): List[WithId[Option[E]]] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM #${tableName}
            WHERE owner = ${userId}"""
        .as(rowParser.*)
    }

  def save(id: String, owner: String, entity: E): WithId[E] =
    db.withConnection { implicit con =>
      val data = entity.asJson.noSpaces
      SQL"""INSERT INTO #${tableName} (id, owner, #${rowEntity})
            VALUES (${id}, ${owner}, $data)
            ON CONFLICT (id) DO UPDATE
            SET #${rowEntity} = $data
            RETURNING *
        """
        .as(rowParser.singleOpt)
        .collect(rowToEntity)
    }.get

  def delete(id: String): Unit =
    db.withConnection { implicit con =>
      SQL"""DELETE FROM #${tableName}
            WHERE id = $id"""
        .executeUpdate()
    }
}
