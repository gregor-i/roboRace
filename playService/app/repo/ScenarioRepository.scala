package repo

import anorm._
import gameLogic.{GameScenario, GameState}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.db.Database

case class ScenarioRow(id: String, owner: String, scenario:GameScenario)

@Singleton
class ScenarioRepository @Inject()(db: Database){
  private val rowParser: RowParser[Option[ScenarioRow]] = for {
    id <- SqlParser.str("id")
    owner <- SqlParser.str("owner")
    maybeScenario <- SqlParser.str("scenario").map(data => decode[GameScenario](data).toOption)
  } yield maybeScenario.map(scenario => ScenarioRow(id, owner, scenario))

  def get(id: String): Option[ScenarioRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
            FROM scenarios
            WHERE id = $id"""
        .as(rowParser.singleOpt).flatten
    }

  def list(): Seq[ScenarioRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM scenarios"""
        .as(rowParser.*).flatten
    }

  def save(row: ScenarioRow): Int = db.withConnection { implicit con =>
    val data = row.scenario.asJson.noSpaces
    SQL"""INSERT INTO scenarios (id, owner, scenario)
          VALUES (${row.id}, ${row.owner}, $data)
          ON CONFLICT (id) DO UPDATE
          SET scenario = $data,
              owner = ${row.owner}"""
      .executeUpdate()
  }

  def delete(id: String): Int = db.withConnection { implicit con =>
    SQL"""DELETE
          FROM scenarios
          WHERE id = $id"""
      .executeUpdate()
  }
}
