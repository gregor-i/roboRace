addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.10")

//resolvers += Resolver.url("io.nhanzlikova.sbt", url("https://dl.bintray.com/geekity/sbt-plugins/"))(Resolver.ivyStylePatterns)
//
//addSbtPlugin("io.nhanzlikova.sbt" % "sbt-embedded-postgres" % "1.1.0")


lazy val root = project.in( file(".") ).dependsOn( assemblyPlugin )
lazy val assemblyPlugin = RootProject(uri("git://github.com/gregor-i/sbt-embedded-postgres"))


// git://github.com/gregor-i/sbt-embedded-postgres
// git://github.com/sbt/sbt-assembly#0.9.1