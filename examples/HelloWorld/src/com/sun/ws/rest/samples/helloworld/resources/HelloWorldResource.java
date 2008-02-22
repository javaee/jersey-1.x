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

package com.sun.ws.rest.samples.helloworld.resources;

import com.sun.ws.rest.impl.json.JSONJAXBContext;
import com.sun.ws.rest.spi.service.ContextResolver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;

// The Java class will be hosted at the URI path "/helloworld"
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;
@Path("/helloworld")
public class HelloWorldResource {
    
   /*@Provider
   public static final class JAXBContextResolver implements ContextResolver<JAXBContext> {
    
    private final JAXBContext context;
    static Class[] beans = {MyBean.class};
    
    public JAXBContextResolver() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
        this.context = new JSONJAXBContext(beans, props);
    }
    
    public JAXBContext getContext(Class<?> objectType) {
        return context;
    }
} */
    
    @XmlRootElement
    public static class MyBean {
        public String greeting;
        public MyBean() {
            greeting = "Hi";
        }
    }

    // The Java method will process HTTP GET requests
    @GET 
    // The Java method will produce content identified by the MIME Media
    // type "text/plain"
    @ProduceMime("text/plain")
    public String getClichedMessage() {
        // Return some cliched textual content
        return "Hello World";
    }
    
    @GET
    @ProduceMime({"application/xml", "application/json"}) 
    @Path("xml")
    public MyBean getXml() {
        return new MyBean();
    }
}