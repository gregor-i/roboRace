name := "roboRace"

version := "0.1"

scalaVersion := "2.12.4"

lazy val playService = project.in(file("playService"))
    .dependsOn(gameLogic)
    .enablePlugins(PlayScala)

lazy val gameLogic = project.in(file("gameLogic"))