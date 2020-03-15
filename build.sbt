import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import scala.sys.process._

name := "roboRace"

scalaVersion in ThisBuild := "2.13.1"

lazy val gameEntities = crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
  .in(file("gameEntities"))

lazy val gameLogic = project.in(file("gameLogic"))
  .dependsOn(gameEntities.jvm)
  .settings( monocle, scalaTest)

lazy val service = project.in(file("service"))
  .dependsOn(gameLogic)
  .settings(scalaTest)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    libraryDependencies += "com.dripower" %% "play-circe" % "2610.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.10",
    libraryDependencies += evolutions,
    libraryDependencies += jdbc,
    libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.5",
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  )
  .enablePlugins(EmbeddedPostgresPlugin)
  .settings(javaOptions += s"-DDATABASE_URL=${postgresConnectionString.value}")

val frontend = project
  .in(file("frontend"))
  .dependsOn(gameEntities.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(scalacOptions += "-P:scalajs:sjsDefinedByDefault")
  .settings(scalaJSUseMainModuleInitializer := true)
//  .settings(snabbdom)
  .settings(circe, monocle)
  .settings(    libraryDependencies +=        "org.scala-js" %%% "scalajs-dom" % "1.0.0")


val frontendIntegration = taskKey[Seq[java.io.File]]("build the frontend and copy the results into service")
//frontendIntegration in ThisBuild := {
//  val frontendJs: Seq[Attributed[sbt.File]] = (frontend / Compile / fastOptJS / webpack).value
//  if (frontendJs.size != 1) {
//      throw new IllegalArgumentException("expected only a single js file")
//    } else {
//      val src = frontendJs.head.data
//      val dest = (baseDirectory in service).value / "public" / "robo-race.js"
//      IO.copy(Seq((src, dest)))
//      Seq(dest)
//    }
//}

compile in Compile := {
//  frontendIntegration.value
  (compile in Compile).value
}

def scalaTest = libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

def circe = {
  val version = "0.13.0"
  libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core"           % version,
    "io.circe" %%% "circe-generic"        % version,
    "io.circe" %%% "circe-generic-extras" % version,
    "io.circe" %%% "circe-parser"         % version,
    "io.circe" %%% "circe-refined"        % version
  )
}

def monocle = {
  val version = "2.0.4"
  libraryDependencies ++= Seq(
    "com.github.julien-truffaut" %%% "monocle-core"    % version,
    "com.github.julien-truffaut" %%% "monocle-macro"   % version,
    "com.github.julien-truffaut" %%% "monocle-unsafe"  % version,
    "com.github.julien-truffaut" %%% "monocle-refined" % version
  )
}

