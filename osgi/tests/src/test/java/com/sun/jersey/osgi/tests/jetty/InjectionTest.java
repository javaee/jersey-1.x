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

package com.sun.jersey.osgi.tests.jetty;

import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.resource.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectionTest extends AbstractJettyWebContainerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @Context HttpServletRequest req;
        
        @Context HttpServletResponse res;
        
        @Context ServletConfig sconf;
        
        @Context ServletContext scont;
        
        @GET
        public String get() {
            assertNotNull(req);
            assertNotNull(res);
            assertNotNull(sconf);
            assertNotNull(scont);
            return "GET";
        }               
    }
        
    @Path("/test")
    public static class HttpMethodConstructorResource {
        
        public HttpMethodConstructorResource(
                @Context HttpServletRequest req,
                @Context HttpServletResponse res, 
                @Context ServletConfig sconf,
                @Context ServletContext scont) {
            assertNotNull(req);
            assertNotNull(res);
            assertNotNull(sconf);
            assertNotNull(scont);            
        }
        
        @GET
        public String get() {
            return "GET";
        }               
    }
    
    @Path("/test")
    @Singleton
    public static class HttpMethodSingletonResource {
        @Context HttpServletRequest req;
        
        @Context HttpServletResponse res;
        
        @Context ServletConfig sconf;
        
        @Context ServletContext scont;
        
        @GET
        public String get() {
            assertNotNull(req);
            assertNotNull(res);
            assertNotNull(sconf);
            assertNotNull(scont);
            return "GET";
        }               
    }
        
    @Path("/test")
    @Singleton
    public static class HttpMethodConstructorSingletonResource {
        
        public HttpMethodConstructorSingletonResource(
                @Context HttpServletRequest req,
                @Context HttpServletResponse res, 
                @Context ServletConfig sconf,
                @Context ServletContext scont) {
            assertNotNull(req);
            assertNotNull(res);
            assertNotNull(sconf);
            assertNotNull(scont);            
        }
        
        @GET
        public String get() {
            return "GET";
        }               
    }

    @Test
    public void testInject() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }    

    @Test
    public void testInjectConstrauctor() {
        startServer(HttpMethodConstructorResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }    

    @Test
    public void testInjectSingleton() {
        startServer(HttpMethodSingletonResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }    

    @Test
    public void testInjectConstructorSingleton() {
        startServer(HttpMethodConstructorSingletonResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }    
}