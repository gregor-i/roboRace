package helper

import gameLogic.{GameFinished, GameRunning, GameStarting, GameState}
import org.scalatest.{Assertion, Matchers}

trait AssertionHelper {
  _: Matchers =>

  def assert(f: GameState => Assertion): GameState => GameState =
    state => {
      f(state)
      state
    }

  def assertStarting(f: GameStarting => Assertion): GameState => GameState =
    state => {
      state shouldBe a[GameStarting]
      f(state.asInstanceOf[GameStarting])
      state
    }

  def assertRunning(f: GameRunning => Assertion): GameState => GameState =
    state => {
      state shouldBe a[GameRunning]
      f(state.asInstanceOf[GameRunning])
      state
    }

  def assertFinished(f: GameFinished => Assertion): GameState => GameState =
    state => {
      state shouldBe a[GameFinished]
      f(state.asInstanceOf[GameFinished])
      state
    }
}
