package gameLogic

import gameEntities._

object DefaultScenario {
  private def robot(index: Int, x: Int, y: Int, direction: Direction): Robot = Robot(index, Position(x, y), direction)
  private def wall(x: Int, y: Int, direction: WallDirection): Wall = Wall(Position(x, y), direction)

  val default = Scenario(
    width = 7,
    height = 9,
    targets = Seq(Position(3, 1)),
    initialRobots = Seq(
      robot(0, 1, 8, Up),
      robot(1, 5, 8, Up),
      robot(2, 2, 8, Up),
      robot(3, 4, 8, Up),
      robot(4, 0, 8, Up),
      robot(5, 6, 8, Up)
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
    ),
    traps = Seq(
      TurnRightTrap(Position(1, 3)),
      TurnLeftTrap(Position(5, 3))
    )
  )
}
