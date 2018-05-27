enablePlugins(PlayScala)

//enablePlugins(EmbeddedPostgresPlugin)

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.dripower" %% "play-circe" % "2609.0"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.2"
libraryDependencies += evolutions
libraryDependencies += jdbc
libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.2"


//postgresSilencer := true
//
//javaOptions += s"-DDATABASE_URL=${postgresConnectionString.value}"