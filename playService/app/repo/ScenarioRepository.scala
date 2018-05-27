package repo

import anorm._
import gameLogic.{GameScenario, GameState}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.db.Database

@Singleton
class ScenarioRepository @Inject()(db: Database){
  private val scenarioParser: RowParser[Option[GameScenario]] = SqlParser.str("scenario").map(data => decode[GameScenario](data).toOption)
  private val rowParser: RowParser[(String, Option[GameScenario])] = for {
    id <- SqlParser.str("id")
    scenario <- scenarioParser
  } yield (id, scenario)


  def get(id: String): Option[GameScenario] =
    db.withConnection { implicit con =>
      SQL"""SELECT scenario
          FROM scenarios
          WHERE id = $id"""
        .as(scenarioParser.singleOpt).flatten
    }

  def list(): Seq[(String, GameScenario)] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM scenarios"""
        .as(rowParser.*)
        .collect { case (id, Some(scenario)) => (id, scenario) }
    }

  def save(id: String, scenario: GameScenario): Unit = db.withConnection { implicit con =>
    val data = scenario.asJson.noSpaces
    SQL"""INSERT INTO scenarios (id, scenario)
          VALUES ($id, $data)
          ON CONFLICT (id) DO UPDATE
          SET scenario = $data"""
      .executeUpdate()
  }
  def delete(id: String): Unit= db.withConnection{ implicit con =>
    SQL"""DELETE
          FROM scenarios
          WHERE id = $id"""
      .executeUpdate()
  }
}
