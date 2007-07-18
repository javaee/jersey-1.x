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

import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ReturnObjectTest extends AbstractBeanTester {
    
    public ReturnObjectTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class ResourceType { 
        @HttpMethod
        public Object get() {
            return new String("TYPE");
        }
    }
        
    @UriTemplate("/")
    static public class ResourceHttpResponse { 
        @HttpMethod
        public Object get() {
            return Response.Builder.representation("HTTP_RESPONSE").type("text/plain").build();
        }        
    }
    
    public void testMethodType() {        
        HttpResponseContext r = call(ResourceType.class, "GET", "/", null, null, "");
        String rep = (String)r.getEntity();
        assertEquals("TYPE", rep);    
    }
        
    public void testMethoResponse() {        
        HttpResponseContext r = call(ResourceHttpResponse.class, "GET", "/", null, null, "");
        String rep = (String)r.getEntity();
        assertEquals("HTTP_RESPONSE", rep);    
    }
}
