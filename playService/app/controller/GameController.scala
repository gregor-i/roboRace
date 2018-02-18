package controller

import javax.inject.{Inject, Singleton}

import gameLogic.{GameNotStarted, GameState}
import play.api.mvc.InjectedController

@Singleton
class GameController @Inject()() extends InjectedController {
//  var gameState: GameState = GameNotStated
//
  def state() = Action{ request =>
    Ok("ok")
  }
}
