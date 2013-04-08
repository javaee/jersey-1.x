/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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