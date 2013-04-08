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

package com.sun.jersey.samples.mandel

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;


object MandelServer {
    private def getPort(defaultPort : Int) = {
        val port = System.getProperty("jersey.test.port");;

        if (null != port)
            try  {
            Integer.parseInt(port);
            } catch {
                case ex: NumberFormatException => defaultPort;
            }
        else
            defaultPort;
    }

    private def getBaseURI() = {
        UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
            path("mandelbrot").build();
    }

    val BASE_URI = getBaseURI();

    def startServer() = {
        val rc = new PackagesResourceConfig("com.sun.jersey.samples.mandel");

        System.out.println("Starting grizzly...");
        GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
    }

    def main(args: Array[String]) {
        val httpServer = startServer();

        println("Server running");
        println("Visit: " + BASE_URI + "/(-2.2,-1.2),(0.8,1.2)");
        println("The query parameter 'limit' specifies the limit on number of iterations");
        println("to determine if a point on the complex plain belongs to the mandelbrot set");
        println("The query parameter 'imageSize' specifies the maximum size of the image");
        println("in either the horizontal or virtical direction");
        println("Hit return to stop...");
        System.in.read();
        println("Stopping server");
        httpServer.stop();
        println("Server stopped");
        System.exit(0);
    }
}
