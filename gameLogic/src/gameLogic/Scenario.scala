package gameLogic

case class Scenario(width: Int, height: Int,
                    targetPosition: Position,
                    initialRobots: Seq[Robot],
                    walls: Seq[Wall],
                    pits: Seq[Position])

object Scenario {
  private def robot(x: Int, y: Int, direction: Direction): Robot = Robot(Position(x, y), direction)
  private def wall(x: Int, y: Int, direction: WallDirection): Wall = Wall(Position(x, y), direction)

  def validation(gameScenario: Scenario): Boolean = {
    val tiles = for {
      x <- 0 until gameScenario.width
      y <- 0 until gameScenario.height
      p = Position(x,y)
      if !gameScenario.pits.contains(p)
    } yield p

    def isDistinct [A](s: Seq[A]): Boolean =
      s.toSet.size == s.size

    Seq(
      gameScenario.initialRobots.nonEmpty,
      gameScenario.height >= 1,
      gameScenario.width >= 1,
      tiles.nonEmpty,
      tiles.contains(gameScenario.targetPosition),
      isDistinct(gameScenario.pits),
      gameScenario.initialRobots.forall(s => tiles.contains(s.position)),
      isDistinct(gameScenario.initialRobots.map(_.position)),
      isDistinct(gameScenario.walls)
    ).forall(identity)
  }

  val default = Scenario(
    width = 7,
    height = 9,
    targetPosition = Position(3, 1),
    initialRobots = Seq(
      robot(1, 8, Up),
      robot(5, 8, Up),
      robot(2, 8, Up),
      robot(4, 8, Up),
      robot(0, 8, Up),
      robot(6, 8, Up)
    ),
    walls = Seq(
      wall(3, 2, Down),
      wall(3, 1, DownRight),
      wall(2, 2, UpRight),
      wall(4, 1, Down),
      wall(2, 1, Down),

      wall(4, 3, DownRight),
      wall(4, 3, Down),

      wall(1, 3, UpRight),
      wall(2, 3, Down),

      wall(0, 6, Down),
      wall(2, 6, Down),
      wall(4, 6, Down),
      wall(6, 6, Down),
    ),
    pits = Seq(
      Position(3, 0),
      Position(1, 5),
      Position(3, 5),
      Position(5, 5)
    )
  )
}

case class Wall(position: Position, direction: WallDirection)
