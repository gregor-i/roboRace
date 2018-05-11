package gameLogic

case class GameScenario(width: Int, height: Int,
                        beaconPosition: Position,
                        targetPosition: Position,
                        initialRobots: Seq[Robot],
                        walls: Seq[Wall],
                        pits: Seq[Position])

object GameScenario {
  private def robot(x: Int, y: Int, direction: Direction): Robot = Robot(Position(x, y), direction)
  private def wall(x: Int, y: Int, direction: WallDirection): Wall = Wall(Position(x, y), direction)

  val default = GameScenario(
    width = 7,
    height = 9,
    beaconPosition = Position(3, 8),
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
      wall(1, 1, Down),
      wall(5, 1, Down),
      wall(2, 1, Down),
      wall(4, 1, Down)
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