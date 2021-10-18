name := "feed-filter"
scalaVersion := "2.13.1"
organization := "eddsteel"

List(Compile -> "com.eddsteel.feedfilter.Main", Test -> "com.eddsteel.feedfilter.TestMain").map {
  case (scope, main) =>
    mainClass in scope := Some(main)
}

fork in run := true

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1"
libraryDependencies += "org.log4s" %% "log4s" % "1.10.0"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"
libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.2"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "1.0.0-M25",
  "org.http4s" %% "http4s-blaze-server" % "1.0.0-M25",
  "org.http4s" %% "http4s-blaze-client" % "1.0.0-M25"
)
libraryDependencies += "co.fs2" %% "fs2-core" % "3.1.2"

Lint.settings
Flags.settings

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.eddsteel.feedfilter.model"
  )
