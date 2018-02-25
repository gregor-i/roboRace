package actor

import actor.GameStateActor._
import akka.actor.{Actor, ActorRef}
import gameLogic.GameState
import gameLogic.command.Command
import gameLogic.processor.Processor
import play.api.Logger

object GameStateActor{
  case class Subscribe(actorRef:ActorRef)
  case class GetState()
}

class GameStateActor(initialState: GameState) extends Actor {
  private var state: GameState = initialState

  private var subscribers: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case command: Command =>
      val nextState = Processor(state)(Seq(command))
      nextState.events.map(_.toString).foreach(s => Logger.info(s"Game emitted Event: $s"))
      subscribers.foreach(_ ! nextState.events)
      state = nextState.state
      sender() ! state

    case Subscribe(actor) => subscribers += actor

    case GetState() => sender() ! state
  }
}