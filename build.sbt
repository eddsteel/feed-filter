name := "feed-filter"
scalaVersion := "2.12.2"
organization := "eddsteel"
version := "slice2-SNAPSHOT"

mainClass in Compile := Some("com.eddsteel.feedfilter.net.Jetty")

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
libraryDependencies += "org.scalatra" %% "scalatra" % "2.5.0"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"
libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106"

wartremoverErrors ++= Warts.allBut(
  Wart.Throw,   // until the EitherT update
  Wart.Nothing, // ditto
  Wart.ImplicitParameter // not really an option with scala Futures.
)

// the bad place
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "net" / "Jetty.scala"
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "net" / "Servlet.scala"
wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "run.scala"
