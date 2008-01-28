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

package com.sun.ws.rest.impl.inject;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.spi.service.ContextResolver;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JAXBContextResolverTest extends AbstractResourceTester {
        
    public JAXBContextResolverTest(String testName) throws Exception {
        super(testName);
    }

    @Provider
    public static class MyJAXBContextResolver implements ContextResolver<JAXBContext> {
        JAXBContext context;
        
        public MyJAXBContextResolver() throws Exception {
            context = JAXBContext.newInstance(MyBean.class);
        }
        
        public JAXBContext getContext(Class<?> objectType) {
            return (objectType == MyBean.class) ? context : null;
        }
    }
    
    @Path("/")
    public static class ContextResource {
        @HttpContext ContextResolver<JAXBContext> cr;
        
        @GET
        public MyBean get() {
            if (cr != null) {
                JAXBContext c = cr.getContext(MyBean.class);
                if (c != null)
                    return new MyBean("GET-WITH-CONTEXT");
                else
                    return new MyBean("GET-WITHOUT-CONTEXT");
            }
            return new MyBean("GET-WITHOUT-CONTEXT");
        }        
        
        @POST
        public MyBean post(MyBean b) {
            if (cr != null) {
                JAXBContext c = cr.getContext(MyBean.class);
                if (c != null)
                    b.value = "POST-WITH-CONTEXT";
                else
                    b.value = "POST-WITHOUT-CONTEXT";                
            } else {
                b.value = "POST-WITHOUT-CONTEXT";                
            }
            return b;
        }        
    }
    
    public void testWithContextResolver() throws IOException {
        initiateWebApplication(ContextResource.class, MyJAXBContextResolver.class);
        
        // TODO need to determine if the JAXBContext returned from 
        // MyJAXBContextResolver is used by JAXB message body readers/writers
        
        MyBean b = resourceProxy("/").get(MyBean.class);
        assertEquals("GET-WITH-CONTEXT", b.value);
        
        b = new MyBean("POST");
        b = resourceProxy("/").post(MyBean.class, b);
        assertEquals("POST-WITH-CONTEXT", b.value);
    }
    
    public void testWithoutContextResolver() throws IOException {
        initiateWebApplication(ContextResource.class);
        
        MyBean b = resourceProxy("/").get(MyBean.class);
        assertEquals("GET-WITHOUT-CONTEXT", b.value);
        
        b = new MyBean("POST");
        b = resourceProxy("/").post(MyBean.class, b);
        assertEquals("POST-WITHOUT-CONTEXT", b.value);
    }
}
