package entities

import scala.util.{Failure, Success, Try}

sealed trait CommandResponse {
  def flatMap(f: Game => CommandResponse): CommandResponse =
    this match {
      case rejection: CommandRejected => rejection
      case CommandAccepted(game)      => f(game)
    }

  def map(f: Game => Game): CommandResponse =
    this match {
      case rejection: CommandRejected => rejection
      case CommandAccepted(game)      => CommandAccepted(f(game))
    }

  def toTry: Try[Game] =
    this match {
      case CommandRejected(reason)   => Failure(new Exception(s"command was rejected: ${reason}"))
      case CommandAccepted(newState) => Success(newState)
    }
}

case class CommandRejected(reason: RejectionReason) extends CommandResponse
case class CommandAccepted(game: Game)              extends CommandResponse
