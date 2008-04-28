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

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.xml.bind.annotation.XmlRootElement;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AcceptableXMLorJSONTest extends AbstractHttpServerTester {
    @XmlRootElement
    public static class JAXBBean {

        public String value; 
        
        public JAXBBean() {}
        
        public JAXBBean(String str) {
            value = str;
        }

        public boolean equals(Object o) {
            if (!(o instanceof JAXBBean)) 
                return false;
            return ((JAXBBean) o).value.equals(value);
        }

        public String toString() {
            return "JAXBClass: "+value;
        }
    }
    
    @Path("/resource")
    public static class Resource {
        @GET
        @ProduceMime({"application/xml", "application/json"})
        public JAXBBean get() {
            return new JAXBBean("test");
        }
    }
        
    public AcceptableXMLorJSONTest(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() {
        startServer(HttpHandler.class, Resource.class);

        WebResource r = Client.create().resource(getUri().path("resource").build());
        r.addFilter(new ClientFilter() {
            public ClientResponse handle(ClientRequest ro) {
                ClientResponse ri = getNext().handle(ro);
                
                assertEquals(200, ri.getStatus());
                assertEquals("application/xml", ri.getMetadata().getFirst("Content-Type"));
                return ri;
            }
        });        
        String content = r.accept("application/xml").get(String.class);
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        content = r.accept("application/*").get(String.class);
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        content = r.accept("*/*").get(String.class);
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));

        
        r.removeAllFilters();
        r.addFilter(new ClientFilter() {
            public ClientResponse handle(ClientRequest ro) {
                ClientResponse ri = getNext().handle(ro);
                
                assertEquals(200, ri.getStatus());
                assertEquals("application/json", ri.getMetadata().getFirst("Content-Type"));
                return ri;
            }
        });
        content = r.accept("application/json").get(String.class);
        assertTrue(content.contains("{\"value\":\"test\"}"));
    }
}