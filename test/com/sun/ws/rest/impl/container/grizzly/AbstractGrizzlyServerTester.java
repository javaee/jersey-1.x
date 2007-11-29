/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.core.ResourceConfig;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractGrizzlyServerTester extends TestCase {
    public static final String CONTEXT = "";

    private final SelectorThread selectorThread = new SelectorThread();

    private int port = 9997;
    
    public AbstractGrizzlyServerTester(String name) {
        super(name);
    }
    
    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT);
    }
    
    public void startServer(Class... resources) {
        start(ContainerFactory.createContainer(Adapter.class, resources));
    }
    
    public void startServer(ResourceConfig config) {
        start(ContainerFactory.createContainer(Adapter.class, config));
    }
    
    public void startServer(String packageName) {
        start(ContainerFactory.createContainer(Adapter.class, packageName));
    }
    
    private void start(Adapter adapter) {
        if (selectorThread.isRunning()){
            stopServer();
        }
        
        selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
        selectorThread.setPort(port);
        selectorThread.setAdapter(adapter);
        try {
            selectorThread.initEndpoint();
        } catch (Exception ex) {
            RuntimeException e = new RuntimeException();
            e.initCause(ex);
            throw e;
        }

        new Thread() {
            public void run() {
                try {
                    selectorThread.startEndpoint();
                } catch (Exception ex) {
                    RuntimeException e = new RuntimeException();
                    e.initCause(ex);
                    throw e;
                }
            }
        }.start();
        
        try {    
            // Wait for the server to start
            Thread.sleep(500); 
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } 
    }
    
    public void stopServer() {
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
    }
    
    public void tearDown() {
        stopServer();
    }
}
