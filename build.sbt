name := "feed-filter"
scalaVersion := "2.12.3"
organization := "eddsteel"

List(Compile -> "com.eddsteel.feedfilter.Main", Test -> "com.eddsteel.feedfilter.TestMain").map {
  case (scope, main) =>
    mainClass in scope := Some(main)
}

fork in run := true

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
libraryDependencies += "org.typelevel" %% "cats-core" % "0.9.0"
libraryDependencies += "org.log4s" %% "log4s" % "1.3.4"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"
libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.0"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.17.0",
  "org.http4s" %% "http4s-blaze-server" % "0.17.0",
  "org.http4s" %% "http4s-blaze-client" % "0.17.0"
)
libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.7"
libraryDependencies += "co.fs2" %% "fs2-cats" % "0.3.0"

Lint.settings
Flags.settings
