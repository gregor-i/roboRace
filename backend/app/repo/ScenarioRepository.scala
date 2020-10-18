package repo

import api.Entity
import entities.Scenario
import io.circe.generic.auto._
import javax.inject.Inject
import play.api.db.Database

class ScenarioRepository @Inject() (db: Database) extends EntityRepo[Entity[Scenario]]("scenarios", "scenario", db)
