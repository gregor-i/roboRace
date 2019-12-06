import gameEntities._
import gameLogic.DefaultScenario
import gameLogic.command.Command
import io.circe.generic.auto._
import io.circe.syntax._

import scala.sys.process._

object Cli {

  def main(args: Array[String]): Unit = {
    val scenario = DefaultScenario.default

    val playerId = "bot"

    val initialGame = Game(
      cycle = 0,
      scenario = scenario,
      players = List.empty,
      events = Seq.empty
    )

    var game = forceAcceptance(Command.registerForGame(0)(playerId)(initialGame))

    println("game started")
    while (!game.events.contains(AllPlayersFinished)) {
      println("calling agent for instructions")
      val agentResponse = callAgent(playerId, game)
      println(s"agent responded with: ${agentResponse}")
      val agentInstructions = parseAgentReponse(agentResponse)
      println(s"parsed agent responded to: ${agentInstructions}")
      game = forceAcceptance(Command.setInstructions(agentInstructions)(playerId)(game))
    }

    println(s"game finished after ${game.cycle} cycles")
  }

  def callAgent(playerId: String, game: Game): String = {
    s"python something '${playerId}' '${game.asJson}'".!!
1  }

  def parseAgentReponse(response: String): Seq[Instruction] = {
    val instructions = response
      .trim
      .split(" ")
      .map(_.toLowerCase)
      .map {
        case "moveforward" => MoveForward
        case "movebackward" => MoveBackward
        case "stepright" => StepRight
        case "stepleft" => StepLeft
        case "movetwiceforward" => MoveTwiceForward
        case "turnright" => TurnRight
        case "turnleft" => TurnLeft
        case "uturn" => UTurn
        case "sleep" => Sleep
        case other => throw new RuntimeException(s"unknown instruction '${other}'")
      }

    if(instructions.length != Constants.instructionsPerCycle)
      throw new RuntimeException(s"invalid number of instructions. required ${Constants.instructionsPerCycle}, got ${instructions.length}")

    instructions
  }

  def forceAcceptance(command: CommandResponse): Game =
    command match {
      case CommandRejected(error) => throw new RuntimeException(s"invalid logic: $error")
      case CommandAccepted(game) => game
    }
}
