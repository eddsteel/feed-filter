// http://scalatra.org//guides/2.5/deployment/standalone.
package com.eddsteel.feedfilter.net

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object Jetty {
  def main(args: Array[String]): Unit = {
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
    val server = new Server(port)
    val context = {
      val ctx = new WebAppContext()
      ctx.setContextPath("/")
      ctx.setResourceBase("src/main/resources")
      ctx.setResourceBase(
        getClass.getClassLoader.getResource("WEB-INF").toExternalForm())
      ctx.setInitParameter(ScalatraListener.LifeCycleKey,
        "com.eddsteel.feedfilter.net.ScalatraBootstrap")
      ctx.addEventListener(new ScalatraListener)
      ctx.addServlet(classOf[DefaultServlet], "/")
      ctx
    }
    server.setHandler(context)

    server.start
    server.join
  }
}
