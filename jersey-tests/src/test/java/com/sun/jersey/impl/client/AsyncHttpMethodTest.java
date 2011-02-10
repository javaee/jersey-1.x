/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jersey.api.client.async.TypeListener;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.impl.container.grizzly.AbstractGrizzlyServerTester;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AsyncHttpMethodTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            sleep();
            return "GET";
        }
               
        @POST
        public String post(String entity) {
            sleep();
            return entity;
        }
        
        @PUT
        public String put(String entity) {
            sleep();
            return entity;
        }
        
        @DELETE
        public String delete() {
            sleep();
            return "DELETE";
        }    

        @Path("wait")
        @GET
        public String getWait() throws InterruptedException {
            sleep();
            sleep();
            sleep();
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

    abstract class X<T> extends TypeListener<T> {
        final List<String> l = new ArrayList<String>();
        final CountDownLatch cdl = new CountDownLatch(1);

        X(Class<T> c) {
            super(c);
        }

        public void check(String s) throws InterruptedException {
            cdl.await();
            assertEquals(1, l.size());
            assertEquals(s, l.get(0));
        }
    }

    class StringListener extends X<String> {
        StringListener() {
            super(String.class);
        }

        public void onComplete(Future<String> f) throws InterruptedException {
            try {
                l.add(f.get());
            } catch (ExecutionException ex) {
                throw new IllegalStateException();
            } finally {
                cdl.countDown();
            }
        }
    }

    class ClientResponseListener extends X<ClientResponse> {
        ClientResponseListener() {
            super(ClientResponse.class);
        }

        public void onComplete(Future<ClientResponse> f) throws InterruptedException {
            try {
                l.add(f.get().getEntity(String.class));
            } catch (ExecutionException ex) {
                throw new IllegalStateException();
            } finally {
                cdl.countDown();
            }
        }
    }

    public AsyncHttpMethodTest(String testName) {
        super(testName);
    }
    
    public void testGet() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class).get());

        terminate(c.getExecutorService());
    }

    public void testGetNotFound() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("404").build());

        boolean caught = false;
        try {
            r.get(String.class).get();
        } catch (ExecutionException ex) {
            caught = ex.getCause() instanceof UniformInterfaceException;
        }
        assertTrue(caught);
        
        terminate(c.getExecutorService());
    }

    public void testGetListener() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        StringListener l = new StringListener();
        Future<?> f = r.get(l);

        assertEquals("GET", f.get());
        l.check("GET");

        terminate(c.getExecutorService());
    }

    public void testGetClientResponseListener() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        ClientResponseListener l = new ClientResponseListener();
        Future<?> f = r.get(l);

        assertTrue(ClientResponse.class.isAssignableFrom(f.get().getClass()));
        l.check("GET");

        terminate(c.getExecutorService());
    }

    public void testGetListenerNotFound() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("404").build());

        final CountDownLatch cdl = new CountDownLatch(1);
        final List<String> l = new ArrayList<String>();
        Future<?> f = r.get(new TypeListener<String>(String.class) {
            public void onComplete(Future<String> t) throws InterruptedException {
                try {
                    l.add(t.get());
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof UniformInterfaceException) {
                        UniformInterfaceException uie = (UniformInterfaceException)ex.getCause();

                        l.add("" + uie.getResponse().getStatus());
                    }
                    cdl.countDown();
                }
            }
        });

        boolean caught = false;
        try {
            f.get();
        } catch (ExecutionException ex) {
            caught = ex.getCause() instanceof UniformInterfaceException;
        }
        assertTrue(caught);

        cdl.await();
        assertEquals(1, l.size());
        assertEquals("404", l.get(0));

        terminate(c.getExecutorService());
    }

    public void testGetClientResponseListenerNotFound() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("404").build());

        final CountDownLatch cdl = new CountDownLatch(1);
        final List<String> l = new ArrayList<String>();
        Future<?> f = r.get(new TypeListener<ClientResponse>(ClientResponse.class) {
            public void onComplete(Future<ClientResponse> t) throws InterruptedException {
                try {
                    l.add("" + t.get().getStatus());
                } catch (ExecutionException ex) {
                    throw new IllegalStateException();
                } finally {
                    cdl.countDown();
                }
            }
        });

        boolean caught = false;
        try {
            f.get();
        } catch (ExecutionException ex) {
            caught = true;
        }
        assertFalse(caught);

        cdl.await();
        assertEquals(1, l.size());
        assertEquals("404", l.get(0));

        terminate(c.getExecutorService());
    }

    public void testPost() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST").get());

        terminate(c.getExecutorService());
    }

    public void testPostListener() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        StringListener l = new StringListener();
        Future<?> f = r.post(l, "POST");

        assertEquals("POST", f.get());
        l.check("POST");

        terminate(c.getExecutorService());
    }


    public void testPut() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT").get());

        terminate(c.getExecutorService());
    }

    public void testPutListener() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        StringListener l = new StringListener();
        Future<?> f = r.put(l, "PUT");

        assertEquals("PUT", f.get());
        l.check("PUT");

        terminate(c.getExecutorService());
    }

    public void testDelete() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class).get());

        terminate(c.getExecutorService());
    }

    public void testDeleteListener() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        StringListener l = new StringListener();
        Future<?> f = r.delete(l);

        assertEquals("DELETE", f.get());
        l.check("DELETE");

        terminate(c.getExecutorService());
    }

    public void testAllSequential() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class).get());

        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST").get());

        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT").get());

        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class).get());

        terminate(c.getExecutorService());
    }

    public void testAllParallel() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        Future<String> get = r.get(String.class);
        Future<String> post = r.post(String.class, "POST");
        Future<String> put = r.put(String.class, "PUT");
        Future<String> delete =  r.delete(String.class);

        assertEquals("GET", get.get());
        assertEquals("POST", post.get());
        assertEquals("PUT", put.get());
        assertEquals("DELETE", delete.get());

        terminate(c.getExecutorService());
    }

    public void testAllParallelListener() throws Exception {
        startServer(HttpMethodResource.class);
        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("test").build());

        final CountDownLatch cdl = new CountDownLatch(4);
        final List<String> l = Collections.synchronizedList(new ArrayList<String>());
        TypeListener<String> al = new TypeListener<String>(String.class) {

            public void onComplete(Future<String> t) throws InterruptedException {
                try {
                    l.add(t.get());
                    cdl.countDown();
                } catch (ExecutionException ex) {
                    throw new IllegalStateException();
                }
            }

        };

        List<Future<String>> lf = new ArrayList<Future<String>>();

        lf.add(r.get(al));
        lf.add(r.post(al, "POST"));
        lf.add(r.put(al, "PUT"));
        lf.add(r.delete(al));

        cdl.await();

        assertEquals(4, l.size());
        assertTrue(l.contains("GET"));
        assertTrue(l.contains("POST"));
        assertTrue(l.contains("PUT"));
        assertTrue(l.contains("DELETE"));

        assertEquals("GET", lf.get(0).get());
        assertEquals("POST", lf.get(1).get());
        assertEquals("PUT", lf.get(2).get());
        assertEquals("DELETE", lf.get(3).get());

        terminate(c.getExecutorService());
    }

    public void testCancelListener() throws Exception {
        startServer(HttpMethodResource.class);

        Client c = Client.create();
        AsyncWebResource r = c.asyncResource(getUri().path("wait").build());

        final CountDownLatch cdl = new CountDownLatch(1);

        final List<Boolean> l = new ArrayList<Boolean>(1);
        Future<?> f = r.get(new TypeListener<String>(String.class) {
            public void onComplete(Future<String> t) throws InterruptedException {
                try {
                    t.get();
                } catch (CancellationException ex) {
                    l.add(true);
                } catch (ExecutionException ex) {
                    throw new IllegalStateException();
                } finally {
                    cdl.countDown();
                }
            }
        });

        f.cancel(true);

        cdl.await();
        assertTrue(f.isCancelled());
        assertEquals(1, l.size());
        assertTrue(l.get(0));

        terminate(c.getExecutorService());
    }

    private void terminate(ExecutorService es) throws Exception {
        es.shutdown();
        assertTrue(es.awaitTermination(100, TimeUnit.MILLISECONDS));
        assertTrue(es.isTerminated());
    }
}