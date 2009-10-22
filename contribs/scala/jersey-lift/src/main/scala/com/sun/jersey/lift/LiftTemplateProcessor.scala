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
import com.sun.jersey.spi.template.TemplateProcessor
import com.sun.jersey.server.impl.container.servlet.RequestDispatcherWrapper

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
class LiftTemplateProcessor(resourceConfig: ResourceConfig) extends TemplateProcessor {
  @Context
  var servletContext: ServletContext = null
  @Context
  var request: HttpServletRequest = null

  def resolve(path: String): String = {
    if (servletContext == null)
      return null

    try {
      // TODO this code actually results in looking up the resource twice
      // once here first then again Lift land 
      // I wonder if there's a better way to do this just once?
      if (servletContext.getResource(path) == null) {
        if (servletContext.getResource(path + ".html") == null &&
            servletContext.getResource(path + ".xhtml") == null) {
          return null
        }
      }
      //Log.debug(() => "Found HTML template " + htmlpath)
      return path
    } catch {
      case e: MalformedURLException =>
      // TODO log
    }
    //Log.debug(() => "No Lift template found for " + path)
    null
  }

  def writeTo(resolvedPath: String, model: AnyRef, out: OutputStream): Unit = {
    ResourceBean.set(request, model)

    // Commit the status and headers to the HttpServletResponse
    out.flush()

    val aPath = if (resolvedPath.startsWith("/")) 
      resolvedPath.substring(1)
    else
      resolvedPath
    val paths = List.fromArray(aPath.split("/"))
    // Log.debug(() => "About to search for Lift resource " + paths)
    
    val template: Box[NodeSeq] = TemplateFinder.findAnyTemplate(paths)
    template match {
      case Full(nodes) => {
        if (request != null) {
          val transformedNodes = S.render(nodes, new HTTPRequestServlet(request))
          val text = transformedNodes.toString()
          out.write(text.getBytes())
        }
        else {
          throw new ContainerException("request was not injected properly by the JAXRS runtime!");
        }
      }

      case other => {
        throw new ContainerException("No template found for " + resolvedPath + " got '" + other + "'");
      }
    }
  }
}