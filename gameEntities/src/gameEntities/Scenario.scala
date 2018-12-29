package gameEntities

case class Scenario(width: Int, height: Int,
                    targetPosition: Position,
                    initialRobots: Seq[Robot],
                    walls: Seq[Wall],
                    pits: Seq[Position],
                    traps: Seq[Trap] = Seq.empty)