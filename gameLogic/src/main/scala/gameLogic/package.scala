import gameEntities.{EventLog, Game}

package object gameLogic {
  implicit class GameLog(game: Game){
    def log(event: EventLog): Game = Lenses.log(event)(game)
  }
}
