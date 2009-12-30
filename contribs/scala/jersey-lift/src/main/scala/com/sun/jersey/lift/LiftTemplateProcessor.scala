package com.sun.jersey.lift

import _root_.java.io.{IOException, OutputStream}
import _root_.java.net.MalformedURLException
import _root_.java.util.Enumeration

import _root_.net.liftweb.common.{Full, Box}
import _root_.net.liftweb.http.{S, LiftServlet, TemplateFinder}
import _root_.net.liftweb.util.Log
import _root_.net.liftweb.http.provider.servlet.HTTPRequestServlet

import javax.ws.rs.Produces
//import org.apache.log4j.Logger
import scala.xml.NodeSeq

import com.sun.jersey.api.core.HttpContext
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.container.ContainerException
import com.sun.jersey.spi.container.servlet.ServletContainer
import com.sun.jersey.api.view.Viewable
import com.sun.jersey.spi.template.ViewProcessor

import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider

/**
 * @version $Revision : 1.1 $
 */
@Provider
@Produces(Array("text/html;qs=5", "text/xhtml"))
class LiftTemplateProcessor extends ViewProcessor[NodeSeq] {
  @Context
  var servletContext: ServletContext = null
  @Context
  var request: HttpServletRequest = null

  def resolve(path: String): NodeSeq = {
    if (servletContext == null)
      return null

    val aPath = if (path.startsWith("/"))
      path.substring(1)
    else
      path
    val paths = List.fromArray(aPath.split("/"))
    // Log.debug(() => "About to search for Lift resource " + paths)

    val template: Box[NodeSeq] = TemplateFinder.findAnyTemplate(paths)
    template match {
      case Full(nodes) => nodes;
      case other => null;
    }

  }

  def writeTo(nodes: NodeSeq, viewable: Viewable, out: OutputStream): Unit = {
    ResourceBean.set(request, viewable.getModel())

    // Commit the status and headers to the HttpServletResponse
    out.flush()

    if (request != null) {
      val transformedNodes = S.render(nodes, new HTTPRequestServlet(request))
      val text = transformedNodes.toString()
      out.write(text.getBytes())
    }
    else {
      throw new ContainerException("request was not injected properly by the JAXRS runtime!");
    }
  }
}