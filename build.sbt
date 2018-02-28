name := "roboRace"

version := "0.1"

scalaVersion := "2.12.4"

cancelable in ThisBuild := true

lazy val playService = project.in(file("playService"))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies += guice,
    libraryDependencies += "com.dripower" %% "play-circe" % "2609.0"
  )
  .dependsOn(gameLogic)

lazy val gameLogic = project.in(file("gameLogic"))
  .settings(folderSettings, scalaTest)

def folderSettings = Seq(
  scalaSource in Compile := baseDirectory.value / "src",
  scalaSource in Test := baseDirectory.value / "test"
)

def scalaTest = libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test