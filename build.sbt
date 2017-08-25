name := "feed-filter"
scalaVersion := "2.12.3"
organization := "eddsteel"
version := "slice11"

List(Compile -> "com.eddsteel.feedfilter.Main", Test -> "com.eddsteel.feedfilter.TestMain").map {
  case (scope, main) =>
    mainClass in scope := Some(main)
}

fork in run := true

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.0-MF"
libraryDependencies += "org.log4s" %% "log4s" % "1.3.4"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"
libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.0"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.16.0a-M1",
  "org.http4s" %% "http4s-blaze-server" % "0.16.0a-M1",
  "org.http4s" %% "http4s-blaze-client" % "0.16.0a-M1"
)
libraryDependencies += "io.verizon.delorean" %% "core" % "1.2.40-scalaz-7.1"

Lint.settings
Flags.settings
