/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.impl.container.grizzly.AbstractGrizzlyServerTester;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author pavel.bucek@oracle.com
 */
public class ThreadpoolSizeTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            sleep();
            return "GET";
        }
               
        private void sleep() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    public ThreadpoolSizeTest(String testName) {
        super(testName);
    }
    
    public void testGet() throws Exception {
        startServer(HttpMethodResource.class);
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_THREADPOOL_SIZE, 10);
        Client c = Client.create(cc);
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        int threadCount = Thread.activeCount();

        List<Future<ClientResponse>> list = new ArrayList<Future<ClientResponse>>(100);

        for(int i = 0; i < 100; i++)
            list.add(r.get(ClientResponse.class));

        for(int i = 0; i < 100; i++) {
            ClientResponse cr = list.get(i).get();
            assertEquals("GET", cr.getEntity(String.class));
            assertTrue((Thread.activeCount() - threadCount) < 20);
        }

        terminate(c.getExecutorService());
    }

    private void terminate(ExecutorService es) throws Exception {
        es.shutdown();
        assertTrue(es.awaitTermination(100, TimeUnit.MILLISECONDS));
        assertTrue(es.isTerminated());
    }
}