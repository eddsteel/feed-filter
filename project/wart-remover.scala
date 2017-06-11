import sbt._
import sbt.Keys._
import wartremover._

object Lint {
  def settings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) ++= Warts.allBut(
      Wart.ImplicitParameter // not really an option with scala Futures.
    ),

    // the bad place
    wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "net" / "Jetty.scala",
    wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "net" / "Servlet.scala",
    wartremoverExcluded += baseDirectory.value / "src" / "main" / "scala" / "run.scala"
  )
}
