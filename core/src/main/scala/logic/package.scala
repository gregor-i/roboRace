import entities.{EventLog, Game}

package object logic {
  implicit class GameLog(game: Game) {
    def log(event: EventLog): Game = Lenses.log(event)(game)
  }
}
