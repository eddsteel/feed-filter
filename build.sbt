name := "feed-filter"
scalaVersion := "2.12.2"
organization := "eddsteel"
version := "slice8"

List(Compile -> "com.eddsteel.feedfilter.net.Jetty", Test -> "com.eddsteel.feedfilter.Main").map {
  case (scope, main) =>
    mainClass in scope := Some(main)
}

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
libraryDependencies += "org.scalatra" %% "scalatra" % "2.5.0"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"
libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106"
libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
libraryDependencies += "org.log4s" %% "log4s" % "1.3.4"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"
libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.0"

Lint.settings
Flags.settings
