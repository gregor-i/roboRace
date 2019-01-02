package gameLogic

import gameEntities._

object ValidateScenario {

  def apply(gameScenario: Scenario): Boolean = {
    val tiles = for {
      x <- 0 until gameScenario.width
      y <- 0 until gameScenario.height
      p = Position(x,y)
      if !gameScenario.pits.contains(p)
    } yield p

    def isDistinct[A](s: Seq[A]): Boolean =
      s.toSet.size == s.size

    Seq(
      gameScenario.initialRobots.nonEmpty,
      gameScenario.height >= 1,
      gameScenario.width >= 1,
      tiles.nonEmpty,
      gameScenario.targets.forall(tiles.contains),
      gameScenario.targets.nonEmpty,
      isDistinct(gameScenario.pits),
      gameScenario.initialRobots.forall(s => tiles.contains(s.position)),
      isDistinct(gameScenario.initialRobots.map(_.position)),
      isDistinct(gameScenario.walls),
      isDistinct(gameScenario.traps.map(_.position)),
      gameScenario.traps.forall(t => tiles.contains(t.position)),
      gameScenario.initialRobots.size <= Constants.maximumNumberOfRobots
    ).forall(identity)
  }
}
