import scala.sys.process._

name := "roboRace"

scalaVersion in ThisBuild := "2.13.3"
scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-Ymacro-annotations")
scalafmtOnCompile in ThisBuild := true

// projects
lazy val root = project
  .in(file("."))
  .aggregate(core.js, core.jvm, frontend, `service-worker`, backend)

lazy val macros = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("macros"))
  .settings(libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .dependsOn(macros)
  .settings(circe, monocle, scalaTest)

lazy val backend = project.in(file("backend"))
  .dependsOn(core.jvm)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += guice,
    libraryDependencies += "com.dripower" %% "play-circe" % "2812.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.14",
    libraryDependencies += evolutions,
    libraryDependencies += jdbc,
    libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.7",
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  )
  .enablePlugins(EmbeddedPostgresPlugin)
  .settings(javaOptions += s"-DDATABASE_URL=${postgresConnectionString.value}")

val frontend = project
  .in(file("frontend"))
  .dependsOn(core.js, macros.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .settings(snabbdom, monocle)
  .settings(    libraryDependencies +=        "org.scala-js" %%% "scalajs-dom" % "1.0.0")

val `service-worker` = project
  .in(file("service-worker"))
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaJSUseMainModuleInitializer := true)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq(
      BuildInfoKey.action("buildTime") { System.currentTimeMillis },
      BuildInfoKey.action("assetFiles") { "ls backend/public".!! }
    )
  )
  .settings(libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0")

// tasks
compile in frontend := {
  val ret           = (frontend / Compile / compile).value
  val buildFrontend = (frontend / Compile / fastOptJS).value.data
  val outputFile    = (backend / baseDirectory).value / "public" / "robo-race.js"
  streams.value.log.info("integrating frontend (fastOptJS)")
  val npmLog = Seq("./node_modules/.bin/browserify", buildFrontend.toString, "-o", outputFile.toString).!!
  streams.value.log.info(npmLog)
  ret
}

stage in frontend := {
  val buildFrontend = (frontend / Compile / fullOptJS).value.data
  val outputFile    = (backend / baseDirectory).value / "public" / "robo-race.js"
  streams.value.log.info("integrating frontend (fullOptJS)")
  val npmLog = Seq("./node_modules/.bin/browserify", buildFrontend.toString, "-o", outputFile.toString).!!
  streams.value.log.info(npmLog)
  outputFile
}

compile in `service-worker` := {
  val ret        = (`service-worker` / Compile / compile).value
  val buildSw    = (`service-worker` / Compile / fastOptJS).value.data
  val outputFile = (backend / baseDirectory).value / "public" / "sw.js"
  streams.value.log.info("integrating service-worker (fastOptJS)")
  val buildLog = Seq("cp", buildSw.toString, outputFile.toString).!!
  streams.value.log.info(buildLog)
  ret
}

stage in `service-worker` := {
  val buildSw    = (`service-worker` / Compile / fullOptJS).value.data
  val outputFile = (backend / baseDirectory).value / "public" / "sw.js"
  streams.value.log.info("integrating service-worker (fullOptJS)")
  val buildLog = Seq("cp", buildSw.toString, outputFile.toString).!!
  streams.value.log.info(buildLog)
  outputFile
}

compile in Compile in root := Def
  .sequential(
    (compile in Compile in frontend),
    (compile in Compile in `service-worker`),
    (compile in Compile in backend)
  )
  .value

stage in root := Def
  .sequential(
    (stage in frontend),
    (stage in `service-worker`),
    (stage in backend)
  )
  .value

test in root := Def
  .sequential(
    test in Test in core.jvm,
    test in Test in core.js,
    test in Test in frontend,
    test in Test in backend
  )
  .value

def snabbdom = Seq(
  resolvers += Resolver.bintrayRepo("gregor-i", "maven"),
  libraryDependencies += "com.github.gregor-i" %%% "scalajs-snabbdom" % "1.0.1"
)

def scalaTest = libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.0" % Test

def circe = {
  val version = "0.13.0"
  libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core"           % version,
    "io.circe" %%% "circe-generic"        % version,
    "io.circe" %%% "circe-generic-extras" % version,
    "io.circe" %%% "circe-parser"         % version
  )
}

def monocle = {
  val version = "2.0.5"
  libraryDependencies ++= Seq(
    "com.github.julien-truffaut" %%% "monocle-core"    % version,
    "com.github.julien-truffaut" %%% "monocle-macro"   % version,
    "com.github.julien-truffaut" %%% "monocle-unsafe"  % version
  )
}

