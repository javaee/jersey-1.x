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

package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.impl.container.grizzly.*;
import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import java.util.concurrent.Future;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import junit.framework.*;

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
        
        private void sleep() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }
        
    public AsyncHttpMethodTest(String testName) {
        super(testName);
    }
    
    public void testGet() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class).get());
    }
    
    public void testPost() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST").get());
    }    
    
    public void testPut() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT").get());
    }
    
    public void testDelete() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class).get());
    }
    
    public void testAllSequential() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class).get());

        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST").get());
        
        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT").get());
        
        r = Client.create().asyncResource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class).get());
    }    
    
    public void testAllParallel() throws Exception {
        startServer(HttpMethodResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().path("test").build());
        
        Future<String> get = r.get(String.class);
        Future<String> post = r.post(String.class, "POST");
        Future<String> put = r.put(String.class, "PUT");
        Future<String> delete =  r.delete(String.class);
        
        assertEquals("GET", get.get());
        assertEquals("POST", post.get());
        assertEquals("PUT", put.get());        
        assertEquals("DELETE", delete.get());
    }    
}