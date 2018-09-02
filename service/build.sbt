enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += "com.dripower" %% "play-circe" % "2609.1"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"
libraryDependencies += evolutions
libraryDependencies += jdbc
libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.2"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

enablePlugins(EmbeddedPostgresPlugin)
javaOptions += s"-DDATABASE_URL=${postgresConnectionString.value}"
postgresSilencer := true
