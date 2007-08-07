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
import com.sun.net.httpserver.HttpServer;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.container.ContainerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AcceptableXMLorJSON extends TestCase {
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
    
    @UriTemplate("/resource")
    public static class WebResource {
        @HttpMethod
        @ProduceMime({"application/xml", "application/json"})
        public JAXBBean get() {
            return new JAXBBean("test");
        }
    }
        
    public AcceptableXMLorJSON(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() throws IOException {
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, WebResource.class);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(9998), 0);
        server.createContext("/context", handler);
        server.setExecutor(null);
        server.start();
                
        String content = get("http://localhost:9998/context/resource", "application/xml");
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        content = get("http://localhost:9998/context/resource", "application/*");
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        content = get("http://localhost:9998/context/resource", "*");
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        
        content = get("http://localhost:9998/context/resource", "application/json");
        assertEquals("{\"jaxbBean\":{\"value\":{\"$\":\"test\"}}}", content);
        
        server.stop(1);
    }
        
    private String get(String uri, String accept) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("GET");
        uc.setRequestProperty("Accept", accept);
        
        assertEquals(200, uc.getResponseCode());
        
        InputStream in = uc.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int r;
        while ((r = in.read(buffer)) != -1) {
            baos.write(buffer, 0, r);
        }
        return new String(baos.toByteArray());
    }    
}
