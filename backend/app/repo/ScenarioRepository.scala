package repo

import anorm._
import entities.Scenario
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.db.Database

case class ScenarioRow(id: String, owner: String, description: String, scenario: Option[Scenario])

@Singleton
class ScenarioRepository @Inject() (db: Database) {
  private val rowParser: RowParser[ScenarioRow] = for {
    id            <- SqlParser.str("id")
    owner         <- SqlParser.str("owner")
    description   <- SqlParser.str("description")
    maybeScenario <- SqlParser.str("scenario").map(data => decode[Scenario](data).toOption)
  } yield ScenarioRow(id, owner, description, maybeScenario)

  def get(id: String): Option[ScenarioRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM scenarios
            WHERE id = $id"""
        .as(rowParser.singleOpt)
    }

  def list(): Seq[ScenarioRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM scenarios
            ORDER BY id"""
        .as(rowParser.*)
    }

  def save(row: ScenarioRow): Int = db.withConnection { implicit con =>
    val data = row.scenario.map(_.asJson.noSpaces)
    SQL"""INSERT INTO scenarios (id, owner, description, scenario)
          VALUES (${row.id}, ${row.owner}, ${row.description}, $data)
          ON CONFLICT (id) DO UPDATE
          SET scenario = $data,
              owner = ${row.owner},
              description = ${row.description}"""
      .executeUpdate()
  }

  def delete(id: String): Int = db.withConnection { implicit con =>
    SQL"""DELETE
          FROM scenarios
          WHERE id = $id"""
      .executeUpdate()
  }
}
