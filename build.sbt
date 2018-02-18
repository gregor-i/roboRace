name := "roboRace"

version := "0.1"

scalaVersion := "2.12.4"

lazy val playService = project.in(file("playService"))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies += guice)
  .dependsOn(gameLogic)

lazy val consoleGame = project.in(file("consoleGame"))
  .settings(folderSettings)
  .dependsOn(gameLogic)

lazy val gameLogic = project.in(file("gameLogic"))
  .settings(folderSettings, scalaTest)

def folderSettings = Seq(
  scalaSource in Compile := baseDirectory.value / "src",
  scalaSource in Test := baseDirectory.value / "test"
)

def scalaTest = libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test