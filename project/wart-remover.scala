import sbt._
import sbt.Keys._
import wartremover._

object Lint {
  def settings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) ++= Warts.all
  )
}
