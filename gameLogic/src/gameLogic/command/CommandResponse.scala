package gameLogic
package command

sealed trait CommandResponse
case class CommandRejected(reason: RejectionReason) extends CommandResponse
case class CommandAccepted(newState: Game) extends CommandResponse
