package gameLogic.gameUpdate

import gameLogic.{EventLog, GameScenario, GameStarting, GameState, Logged, RejectionReason, StartingPlayer}
import org.scalatest.{Assertion, Matchers}

trait UpdateChain {
  _: Matchers =>
  type ChainElement = GameState => GameState

  val p1 = "p1"
  val p2 = "p2"
  val p3 = "p3"

  val cycle: GameState => Logged[GameState] = s => Cycle(s)

  def updateChain(state: GameState)(fs: ChainElement*): GameState =
    fs.foldLeft(state)((s, f) => f(s))

  implicit class EnrichLogged(val f: GameState => Logged[GameState]){
    def logged(assets: Seq[EventLog] => Assertion = _ => succeed) : ChainElement =
      state => {
        val r = f(state)
        assets(r.events)
        r.state
      }
  }

  implicit class EnrichCommandReponse(val f: GameState => CommandResponse) {
    def accepted: ChainElement = state => {
      val r = f(state)
      r shouldBe a[CommandAccepted]
      r.asInstanceOf[CommandAccepted].newState
    }

    def rejected(): ChainElement =
      state => {
        val r = f(state)
        r shouldBe a[CommandRejected]
        state
      }

    def rejected(expectedReason: RejectionReason): ChainElement =
      state => {
        val r = f(state)
        r shouldBe a[CommandRejected]
        r.asInstanceOf[CommandRejected].reason shouldBe expectedReason
        state
      }
  }
}
