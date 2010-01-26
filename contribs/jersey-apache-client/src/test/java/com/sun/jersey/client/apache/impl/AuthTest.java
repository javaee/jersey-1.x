/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.client.apache.impl;

import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AuthTest extends AbstractGrizzlyServerTester {

    public AuthTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    public static class PreemptiveAuthResource {
        @GET
        public String get(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return e;
        }
    }
        
    public void testPreemptiveAuth() {
        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getState().setCredentials(null, null, -1, "name", "password");
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().build());
        assertEquals("GET", r.get(String.class));
    }

    public void testPreemptiveAuthPost() {
        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getState().setCredentials(null, null, -1, "name", "password");
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

    @Path("/test")
    @Singleton
    public static class AuthResource {
        int requestCount = 0;
        @GET
        public String get(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return e;
        }

    }

    public void testAuthGet() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getState().setCredentials(null, null, -1, "name", "password");
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }

    public void testAuthPost() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getState().setCredentials(null, null, -1, "name", "password");
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

    static class BasicCredentialsProvider implements CredentialsProvider {
        private boolean called;

        public Credentials getCredentials(AuthScheme arg0, String arg1, int arg2, boolean arg3) throws CredentialsNotAvailableException {
            called = true;
            return new UsernamePasswordCredentials ("name",
                                                    "password");
        }

        boolean isCalled() {
            return called;
        }
    }

    public void testAuthInteractiveGet() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_INTERACTIVE, true);
        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_CREDENTIALS_PROVIDER, bcp);
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
        assertTrue(bcp.isCalled());
    }

    public void testAuthInteractivePost() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_INTERACTIVE, true);
        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_CREDENTIALS_PROVIDER, bcp);
        ApacheHttpClient c = ApacheHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
        assertTrue(bcp.isCalled());
    }
}
