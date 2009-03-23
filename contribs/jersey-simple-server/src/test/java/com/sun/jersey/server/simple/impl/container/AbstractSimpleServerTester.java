/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.jersey.server.simple.impl.container;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.ws.rs.core.UriBuilder;

import junit.framework.TestCase;

import org.simpleframework.http.core.Container;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * This is used to create a test that has the capability to start a 
 * server that listens to an ephemeral port. Starting servers using the
 * ephemeral ports ensures multiple servers can be started and stopped
 * within the same VM. This also provides a {@link UriBuilder} that
 * enables the server to be connected to from within the test.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractSimpleServerTester extends TestCase {
   
    public static final String CONTEXT = "";

    private InetSocketAddress address;
    private Connection connection;
    
    public AbstractSimpleServerTester(String name) {
        super(name);
    }
    
    /**
     * Create a {@link UriBuilder} which will contain the ephemeral port
     * that the server is connected to. This ensures that the tests will
     * know how to connect to the server.
     * 
     * @return a target to connect to the server
     * 
     * @throws IOException thrown if the server has not been started
     */
    public UriBuilder getUri() {
       if(connection == null) {
          throw new RuntimeException("Server has not been started");
       }
       String base = "http://localhost";
       int port = address.getPort();
       System.out.println(port);
       
       return UriBuilder.fromUri(base).port(port).path(CONTEXT);
    }
    
    public void startServer(Class... resources) {
        start(ContainerFactory.createContainer(Container.class, resources));
    }
    
    public void startServer(ResourceConfig config) {
        start(ContainerFactory.createContainer(Container.class, config));
    }
    
    /**
     * This will start the server and connection to an ephemeral port
     * to ensure that there is no conflict when starting and stopping
     * multiple {@link Server} instances in the same VM.
     * 
     * @throws RuntimeException if the server has already started
     */
    protected void start(Container container) {
        if (connection != null) {
            stopServer();
        }
        try {
           connection = new SocketConnection(container);
           address = (InetSocketAddress)connection.connect(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void stopServer() {
        try {
           connection.close();
           connection = null;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void tearDown() {
        stopServer();
    }
}
