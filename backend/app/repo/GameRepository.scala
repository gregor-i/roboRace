package repo

import entities.Game
import io.circe.generic.auto._
import javax.inject.Inject
import play.api.db.Database

class GameRepository @Inject() (db: Database) extends EntityRepo[Game]("games", "game", db)
