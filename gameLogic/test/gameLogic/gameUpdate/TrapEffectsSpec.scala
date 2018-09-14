package gameLogic.gameUpdate

import gameLogic._
import helper.GameUpdateHelper
import org.scalatest.{FunSuite, Matchers}

class TrapEffectsSpec extends FunSuite with Matchers with GameUpdateHelper {
  test("TurnRightTrap: should turn robots before the cycle starts") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      addTrap(TurnRightTrap(Position(1, 8))),
      forcedInstructions(p0)(MoveForward),
      assertCycle(1),
      assertLog(_ should contain(TrapEffect(0, TurnRightTrap(Position(1, 8))))),
      assertLog(_ should contain(RobotTurns(0, Position(1, 8), Up, UpRight))),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, UpRight, Position(1, 8), Position(2, 8))))))
    )
  }

  test("TurnLeftTrap: should turn robots before the cycle starts") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      addTrap(TurnLeftTrap(Position(1, 8))),
      forcedInstructions(p0)(MoveForward),
      assertCycle(1),
      assertLog(_ should contain(TrapEffect(0, TurnLeftTrap(Position(1, 8))))),
      assertLog(_ should contain(RobotTurns(0, Position(1, 8), Up, UpLeft))),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, UpLeft, Position(1, 8), Position(0, 8))))))
    )
  }

  test("TurnLeftTrap: should turn a robot which moves onto it") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      addTrap(TurnRightTrap(Position(1, 7))),
      forcedInstructions(p0)(MoveForward, MoveForward),
      assertCycle(1),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, Up, Position(1, 8), Position(1, 7)))))),
      assertLog(_ should contain(TrapEffect(0, TurnRightTrap(Position(1, 7))))),
      assertLog(_ should contain(RobotTurns(0, Position(1, 7), Up, UpRight))),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, UpRight, Position(1, 7), Position(2, 7))))))
    )
  }

  test("StunTrap: should remove the first instruction if a robot starts it's cycle on the StunTrap") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      addTrap(StunTrap(Position(1, 8))),
      forcedInstructions(p0)(MoveForward, MoveForward),
      assertCycle(1),
      assertLog(_ should contain(TrapEffect(0, StunTrap(Position(1, 8))))),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, Up, Position(1, 8), Position(1, 7)))))),
      assertLog(_ should not contain RobotMoves(Seq(RobotPositionTransition(0, Up, Position(1, 7), Position(1, 6)))))
    )
  }

  test("StunTrap: should remove the next instruction if a robot moves onto the trap") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      addTrap(StunTrap(Position(1, 7))),
      forcedInstructions(p0)(MoveForward, MoveForward),
      assertCycle(1),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, Up, Position(1, 8), Position(1, 7)))))),
      assertLog(_ should contain(TrapEffect(0, StunTrap(Position(1, 7))))),
      assertLog(_ should not contain RobotMoves(Seq(RobotPositionTransition(0, Up, Position(1, 7), Position(1, 6)))))
    )
  }

  test("StunTrap: should have no effect if there are no instructions left") {
    sequenceWithAutoCycle(createGame(Scenario.default)(p0))(
      addTrap(StunTrap(Position(1, 7))),
      forcedInstructions(p0)(Sleep, Sleep, Sleep, Sleep, MoveForward),
      assertCycle(1),
      assertLog(_ should contain(RobotMoves(Seq(RobotPositionTransition(0, Up, Position(1, 8), Position(1, 7)))))),
      assertLog(_ should contain(TrapEffect(0, StunTrap(Position(1, 7)))))
    )
  }
}
