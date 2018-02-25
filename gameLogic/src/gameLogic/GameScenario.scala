package gameLogic

case class GameScenario(width: Int, height: Int,
                        beaconPosition: Position,
                        targetPosition: Position,
                        initialRobots: Map[Int, Robot],
                        walls: Seq[Wall])

object GameScenario {
  private def robot(x: Int, y: Int, direction: Direction): Robot = Robot(Position(x, y), direction)

  val default = GameScenario(
    width = 7,
    height = 9,
    beaconPosition = Position(3, 8),
    targetPosition = Position(3, 1),
    initialRobots = Map(
      0 -> robot(1, 8, Up),
      1 -> robot(5, 8, Up),
      2 -> robot(2, 8, Up),
      3 -> robot(4, 8, Up),
      4 -> robot(0, 8, Up),
      5 -> robot(6, 8, Up)
    ),
    walls = Seq()
  )
}

case class Wall(position: Position, direction: WallDirection)