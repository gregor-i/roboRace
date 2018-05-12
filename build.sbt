name := "roboRace"

scalaVersion := "2.12.4"

lazy val playService = project.in(file("playService"))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    libraryDependencies += ws,
    libraryDependencies += "com.dripower" %% "play-circe" % "2609.0"
  )
  .dependsOn(gameLogic)

lazy val gameLogic = project.in(file("gameLogic"))
  .settings(folderSettings, monocle, scalaTest)

def folderSettings = Seq(
  scalaSource in Compile := baseDirectory.value / "src",
  scalaSource in Test := baseDirectory.value / "test"
)

def scalaTest = libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test

def monocle = Seq(libraryDependencies ++= Seq(
  "com.github.julien-truffaut" %% "monocle-core" % "1.5.0",
  "com.github.julien-truffaut" %% "monocle-macro" % "1.5.0",
  "com.github.julien-truffaut" %% "monocle-law" % "1.5.0" % Test
),
  addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full))