import java.io.FileWriter

import gameLogic._
import gameLogic.command.DefineScenario
import gameLogic.{GameNotDefined, GameState}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._


val x = DefineScenario(GameScenario.default)

val file = new java.io.File("DefineScenario.json")
val writer = new FileWriter(file)
writer.append(x.asJson.spaces2)
writer.close()

