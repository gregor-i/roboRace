package repo

import entities.Scenario
import io.circe.generic.auto._
import javax.inject.Inject
import play.api.db.Database

class ScenarioRepository @Inject() (db: Database) extends EntityRepo[Scenario]("scenarios", "scenario", db)
