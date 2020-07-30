import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import scala.sys.process._

name := "roboRace"

scalaVersion in ThisBuild := "2.13.1"
scalafmtOnCompile in ThisBuild := true

lazy val gameEntities = crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
  .in(file("gameEntities"))
  .settings(circe, monocle, scalaTest)

lazy val service = project.in(file("service"))
  .dependsOn(gameEntities.jvm)
//  .settings(scalaTest)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    libraryDependencies += "com.dripower" %% "play-circe" % "2812.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.10",
    libraryDependencies += evolutions,
    libraryDependencies += jdbc,
    libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.5",
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  )
  .enablePlugins(EmbeddedPostgresPlugin)
  .settings(javaOptions += s"-DDATABASE_URL=${postgresConnectionString.value}")

val frontend = project
  .in(file("frontend"))
  .dependsOn(gameEntities.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .settings(snabbdom, monocle)
  .settings(    libraryDependencies +=        "org.scala-js" %%% "scalajs-dom" % "1.0.0")

compile in frontend := {
  val ret           = (frontend / Compile / compile).value
  val buildFrontend = (frontend / Compile / fastOptJS).value.data
  val outputFile    = (service / baseDirectory).value / "public" / "robo-race.js"
  streams.value.log.info("integrating frontend (fastOptJS)")
  val npmLog = Seq("./node_modules/.bin/browserify", buildFrontend.toString, "-o", outputFile.toString).!!
  streams.value.log.info(npmLog)
  ret
}

def snabbdom = Seq(
  resolvers += Resolver.bintrayRepo("gregor-i", "maven"),
  libraryDependencies += "com.github.gregor-i" %%% "scalajs-snabbdom" % "1.0"
)

def scalaTest = libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.0" % Test

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

