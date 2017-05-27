name := "feed-filter"
scalaVersion := "2.12.2"
organization := "eddsteel"
version := "slice3"

List(
  Compile -> "com.eddsteel.feedfilter.net.Jetty",
  Test -> "com.eddsteel.feedfilter.Main").map {
  case (scope, main) =>
    mainClass in scope := Some(main)
}

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
libraryDependencies += "org.scalatra" %% "scalatra" % "2.5.0"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"
libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106"
libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"

wartremoverErrors ++= Warts.allBut(
  Wart.ImplicitParameter // not really an option with scala Futures.
)

// the bad place
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "net" / "Jetty.scala"
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "net" / "Servlet.scala"
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "run.scala"

// scalafmt for 0.13
def latestScalafmt = "0.7.0-RC1"
commands += Command.args("scalafmt", "Run scalafmt cli.") {
  case (state, args) =>
    val Right(scalafmt) =
      org.scalafmt.bootstrap.ScalafmtBootstrap.fromVersion(latestScalafmt)
    scalafmt.main("--non-interactive" +: args.toArray)
    state
}
commands += Command.args("scalafmtDiff", "Run scalafmt on changed files.") {
  case (state, args) =>
    val Right(scalafmt) =
      org.scalafmt.bootstrap.ScalafmtBootstrap.fromVersion(latestScalafmt)
    scalafmt.main("--non-interactive" +: "--diff" +: args.toArray)
    state
}
