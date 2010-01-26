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

package com.sun.jersey.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
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
        @Produces({"application/xml", "application/json"})
        public JAXBBean get() {
            return new JAXBBean("test");
        }
    }
        
    public AcceptableXMLorJSONTest(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() {
        startServer(Resource.class);

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