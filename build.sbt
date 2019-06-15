name := "de.ungefroren.discord.Minzomat"

version := "1.0"

scalaVersion := "2.12.8"

mainClass := Some("de.ungefroren.discord.Minzomat.Launcher")

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  s"Minzomat-${module.revision}.${artifact.extension}"
}

//akka actors
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.23"

// log4j Logger
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.22"

// JDA
resolvers += "jcenter-bintray" at "http://jcenter.bintray.com"
libraryDependencies += "net.dv8tion" % "JDA" % "3.8.3_463"