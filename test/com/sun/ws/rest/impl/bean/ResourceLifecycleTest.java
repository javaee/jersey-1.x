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

package com.sun.ws.rest.impl.bean;

import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.HttpRequestContextImpl;
import com.sun.ws.rest.impl.HttpResponseContextImpl;
import com.sun.ws.rest.impl.MultivaluedMapImpl;
import com.sun.ws.rest.impl.TestHttpRequestContext;
import com.sun.ws.rest.impl.application.WebApplicationImpl;
import com.sun.ws.rest.spi.resource.PerRequest;
import com.sun.ws.rest.spi.resource.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import junit.framework.TestCase;

/**
 *
 * @author Marc Hadley
 */
public class ResourceLifecycleTest extends TestCase {
    
    @UriTemplate("foo")
    @Singleton
    public static class TestFooBean {
        
        private int count;
        
        public TestFooBean() {
            this.count = 0;
        }
        
        @HttpMethod("GET")
        public String doGet() {
            count++;
            return Integer.toString(count);
        }
        
    }
    
    @UriTemplate("bar")
    @PerRequest
    public static class TestBarBean {
        
        private int count;
        
        public TestBarBean() {
            this.count = 0;
        }
        
        @HttpMethod("GET")
        public String doGet() {
            count++;
            return Integer.toString(count);
        }
        
    }
    
    @UriTemplate("baz")
    public static class TestBazBean {
        
        private int count;
        
        public TestBazBean() {
            this.count = 0;
        }
        
        @HttpMethod("GET")
        public String doGet() {
            count++;
            return Integer.toString(count);
        }
        
    }
    
    WebApplicationImpl a;
    
    public ResourceLifecycleTest(String testName) {
        super(testName);

        final Set<Class> r = new HashSet<Class>();
        r.add(TestFooBean.class);
        r.add(TestBarBean.class);
        r.add(TestBazBean.class);
        
        a = new WebApplicationImpl();
        ResourceConfig c = new ResourceConfig() {
            public Set<Class> getResourceClasses() {
                return r;
            }

            public boolean isIgnoreMatrixParams() {
                return true;
            }

            public boolean isRedirectToNormalizedURI() {
                return true;
            }
        };

        a.initiate(null, c);

    }
    
    public void testOneWebResource() {
        String count;
        
        count = doGET("foo");
        assertEquals(count, "1");
        count = doGET("foo");
        assertEquals(count, "2");
        count = doGET("foo");
        assertEquals(count, "3");

        count = doGET("bar");
        assertEquals(count, "1");
        count = doGET("bar");
        assertEquals(count, "1");
        count = doGET("bar");
        assertEquals(count, "1");
        
        count = doGET("baz");
        assertEquals(count, "1");
        count = doGET("baz");
        assertEquals(count, "1");
        count = doGET("baz");
        assertEquals(count, "1");        
    }
    
    public String doGET(String path) {
        ByteArrayInputStream e = new ByteArrayInputStream("".getBytes());
        final HttpRequestContextImpl request = new TestHttpRequestContext("GET", e, path, "/base/", path);

        HttpResponseContextImpl response = new HttpResponseContextImpl(request) {
            public OutputStream getOutputStream() throws IOException {
                throw new UnsupportedOperationException();
            }
        };

        a.handleRequest(request, response);        
        String retVal = (String)response.getEntity();
        return retVal;
    }
}
