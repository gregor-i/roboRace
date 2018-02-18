import gameLogic.action._
import gameLogic.command.{DefineNextAction, RegisterForGame, StartGame}
import gameLogic.eventLog.EventLog
import gameLogic.processor.Processor
import gameLogic.{GameScenario, GameState}

object ConsoleGame {

  var gameState: GameState = GameState.initalState
  val player1 = "player 1"
  val player2 = "player 2"

  def stringToAction(s: String): Option[Action] = s match {
    case "MoveForward" => Some(MoveForward)
    case "MoveBackward" => Some(MoveBackward)
    case "TurnRight" => Some(TurnRight)
    case "TurnLeft" => Some(TurnLeft)
    case _ => None
  }

  def inputAction(introString: String): Action = {
    while (true) {
      println(introString)
      val input = stringToAction(scala.io.StdIn.readLine())
      if (input.isDefined)
        return input.get
    }
    throw new Error
  }

  def outputEvents(events: Seq[EventLog]): Unit =
    events.foreach(event => println("events: " + event))

  def main(args: Array[String]): Unit = {
    val initializedState = Processor(gameState)(Seq(
      RegisterForGame(player1),
      RegisterForGame(player2),
      StartGame(GameScenario.default)
    ))

    outputEvents(initializedState.events)
    gameState = initializedState.state

    while (true) {
      val action1 = inputAction("input Operation player 1: (TurnLeft, TurnRight, MoveForward, MoveBackward)")
      val action2 = inputAction("input Operation player 2: (TurnLeft, TurnRight, MoveForward, MoveBackward)")
      val nextState = Processor(gameState)(Seq(DefineNextAction(player1, action1), DefineNextAction(player2, action2)))

      outputEvents(nextState.events)
      gameState = nextState.state
    }
  }
}
