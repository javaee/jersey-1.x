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

package net.liftweb.jersey.test

import _root_.org.mortbay.jetty.Server
//import _root_.org.mortbay.jetty.servlet.Context
import _root_.org.mortbay.jetty.servlet.ServletHolder
import _root_.org.mortbay.jetty.webapp.WebAppContext

import _root_.net.sourceforge.jwebunit.junit.WebTester
import _root_.junit.framework.AssertionFailedError

object JettyTestServer {
  private val serverPort_ = System.getProperty("SERVLET_PORT", "8989").toInt
  private var baseUrl_ = "http://localhost:" + serverPort_

  private val server_ : Server = {
    val server = new Server(serverPort_)
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setWar("src/main/webapp")
    //val context = new Context(_server, "/", Context.SESSIONS)
    //context.addFilter(new FilterHolder(new LiftFilter()), "/");
    server.addHandler(context)
    server
  }

  def urlFor(path: String) = baseUrl_ + path

  def start() = server_.start()

  def stop() = {
    server_.stop()
    server_.join()
  }

  def browse(startPath: String, f:(WebTester) => Unit) = {
    val wc = new WebTester()
    try {
      wc.setScriptingEnabled(false)
      wc.beginAt(JettyTestServer.urlFor(startPath))
      f(wc)
    } catch {
      case exc: AssertionFailedError => {
        System.err.println("serveur response: ", wc.getServeurResponse())
        throw exc
      }
    } finally {
      wc.closeBrowser()
    }
  }

}

