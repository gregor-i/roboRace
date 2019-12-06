name := "roboRace"

ThisBuild / scalaVersion := "2.12.8"

lazy val gameEntities = project.in(file("gameEntities"))
  .settings(folderSettings)

lazy val gameLogic = project.in(file("gameLogic"))
  .settings(folderSettings, monocle, scalaTest)

lazy val cli = project.in(file("cli"))
  .settings(folderSettings)
  .dependsOn(gameLogic)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % "0.11.0",
      "io.circe" %%% "circe-generic" % "0.11.0",
      "io.circe" %%% "circe-parser" % "0.11.0")
  )

lazy val service = project.in(file("service"))
  .dependsOn(gameLogic)
  .settings(scalaTest)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    libraryDependencies += "com.dripower" %% "play-circe" % "2610.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5",
    libraryDependencies += evolutions,
    libraryDependencies += jdbc,
    libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.2",
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  )
  .enablePlugins(EmbeddedPostgresPlugin)
  .settings(javaOptions += s"-DDATABASE_URL=${postgresConnectionString.value}")

lazy val frontend = project.in(file("frontend"))
  .settings(folderSettings)
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaJSUseMainModuleInitializer := true)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(skip in packageJSDependencies := false)
  .settings(emitSourceMaps := false)
  .settings(snabbdom)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % "0.11.0",
      "io.circe" %%% "circe-generic" % "0.11.0",
      "io.circe" %%% "circe-parser" % "0.11.0")
  )


val frontendIntegration = taskKey[Seq[java.io.File]]("build the frontend and copy the results into service")
frontendIntegration in ThisBuild := {
  val frontendJs: Seq[Attributed[sbt.File]] = (frontend / Compile / fastOptJS / webpack).value
  if (frontendJs.size != 1) {
      throw new IllegalArgumentException("expected only a single js file")
    } else {
      val src = frontendJs.head.data
      val dest = (baseDirectory in service).value / "public" / "robo-race.js"
      IO.copy(Seq((src, dest)))
      Seq(dest)
    }
}

compile in Compile := {
  frontendIntegration.value
  (compile in Compile).value
}

def folderSettings = Seq(
  scalaSource in Compile := baseDirectory.value / "src",
  scalaSource in Test := baseDirectory.value / "test"
)

def scalaTest = libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

def monocle =
  libraryDependencies ++= Seq(
    "com.github.julien-truffaut" %% "monocle-core" % "1.5.0",
    "com.github.julien-truffaut" %% "monocle-macro" % "1.5.0",
    "com.github.julien-truffaut" %% "monocle-unsafe" % "1.5.0"
  )

def snabbdom = Seq(
  libraryDependencies += "com.raquo" %%% "snabbdom" % "0.1.1",
  npmDependencies in Compile += "snabbdom" -> "0.7.0"
)
