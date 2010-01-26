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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ConsumeProduceSimpleTest extends AbstractResourceTester {
    
    public ConsumeProduceSimpleTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    @Consumes("text/html")
    public static class ConsumeSimpleBean {
        @POST
        public void doPostHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Content-Type"));
            response.setResponse(Response.ok("HTML").build());
        }
        
        @POST
        @Consumes("text/xhtml")
        public void doPostXHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Content-Type"));
            response.setResponse(Response.ok("XHTML").build());
        }
    }
        
    @Path("/{arg1}/{arg2}")
    @Produces("text/html")
    public static class ProduceSimpleBean {
        @GET
        public void doGetHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
            response.setResponse(Response.ok("HTML").build());
        }
        
        @GET
        @Produces("text/xhtml")
        public void doGetXhtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Accept"));
            response.setResponse(Response.ok("XHTML").build());
        }
    }
    
    @Path("/{arg1}/{arg2}")
    @Consumes("text/html")
    @Produces("text/html")
    public static class ConsumeProduceSimpleBean {
        @GET
        public void doGetHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
            response.setResponse(Response.ok("HTML").build());
        }
        
        @GET
        @Produces("text/xhtml")
        public void doGetXhtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Accept"));
            response.setResponse(Response.ok("XHTML").build());
        }
        
        @POST
        public void doPostHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Content-Type"));
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
            response.setResponse(Response.ok("HTML").build());
        }
        
        @POST
        @Consumes("text/xhtml")
        @Produces("text/xhtml")
        public void doPostXHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Content-Type"));
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Accept"));
            response.setResponse(Response.ok("XHTML").build());
        }
    }
    
    public void testConsumeSimpleBean() {
        initiateWebApplication(ConsumeSimpleBean.class);
        WebResource r = resource("/a/b");
        
        assertEquals("HTML", r.entity("", "text/html").post(String.class));
        assertEquals("XHTML", r.entity("", "text/xhtml").post(String.class));
    }
    
    public void testProduceSimpleBean() {
        initiateWebApplication(ProduceSimpleBean.class);
        WebResource r = resource("/a/b");

        assertEquals("HTML", r.accept("text/html").get(String.class));
        assertEquals("XHTML", r.accept("text/xhtml").get(String.class));
    }
    
    public void testConsumeProduceSimpleBean() {
        initiateWebApplication(ConsumeProduceSimpleBean.class);
        WebResource r = resource("/a/b");
        
        assertEquals("HTML", r.entity("", "text/html").accept("text/html").post(String.class));
        assertEquals("XHTML", r.entity("", "text/xhtml").accept("text/xhtml").post(String.class));
        assertEquals("HTML", r.accept("text/html").get(String.class));
        assertEquals("XHTML", r.accept("text/xhtml").get(String.class));
    }
    
    @Path("/")
    @Consumes("text/html")
    @Produces("text/plain")
    public static class ConsumeProduceWithParameters {
        @POST
        public String post(String in, @Context HttpHeaders h) {
            return h.getMediaType().getParameters().toString();
        }
    }
    
    public void testProduceWithParameters() {
        initiateWebApplication(ConsumeProduceWithParameters.class);
        WebResource r = resource("/",false);

        assertEquals("{a=b, c=d}", r.type("text/html;a=b;c=d").
                post(String.class, "<html>content</html>"));
    }
    
}
