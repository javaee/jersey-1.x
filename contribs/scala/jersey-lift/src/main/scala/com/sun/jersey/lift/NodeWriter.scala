package com.sun.jersey.lift

import _root_.java.io.OutputStream
import _root_.java.lang.annotation.Annotation
import _root_.java.lang.reflect.Type

import _root_.net.liftweb.http.{S, LiftServlet}
import _root_.net.liftweb.util.{Full, Box, Log}
import _root_.net.liftweb.http.provider.servlet.HTTPRequestServlet

import javax.servlet.ServletContext
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import javax.ws.rs.core.{UriInfo, Context, MultivaluedMap, MediaType}
import javax.ws.rs.ext.{MessageBodyWriter, Provider}

import com.sun.jersey.api.container.ContainerException
import com.sun.jersey.api.core.HttpContext

import javax.ws.rs.Produces
import scala.xml.NodeSeq

/**
 * Converts a Scala   { @link NodeSeq } to a String for rendering nodes as HTML, XML, XHTML using LiftWeb's templates
 *
 * @version $Revision : 1.1 $
 */
@Provider
@Produces(Array("text/html;qs=5", "text/xhtml"))
class NodeWriter extends MessageBodyWriter[NodeSeq] {
  @Context
  var request: HttpServletRequest = null
  @Context
  var uriInfo: UriInfo = null

  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[NodeSeq].isAssignableFrom(aClass)
  }

  def getSize(nodes: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L

  def writeTo(template: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, multiMap: MultivaluedMap[String, Object], out: OutputStream): Unit = {
    // Commit the status and headers to the HttpServletResponse
    out.flush

    if (request != null) {
      if (uriInfo != null) {
        val resources = uriInfo.getMatchedResources
        if (!resources.isEmpty) {
          val it = resources.get(resources.size() - 1)
          ResourceBean.set(request, it)
        }
      }
      val nodes = S.render(template, new HTTPRequestServlet(request))
      val text = nodes.toString();
      out.write(text.getBytes());
    }
    else {
      throw new ContainerException("request was not injected properly by the JAXRS runtime!");
    }
  }
}
