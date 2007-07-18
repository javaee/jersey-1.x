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

package com.sun.ws.rest.impl.methodparams;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.bean.AbstractBeanTester;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TypeReturnTest extends AbstractBeanTester {
    
    public TypeReturnTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/")
    static public class Resource { 
        @HttpMethod("GET")
        public String doGet() {
            return "CONTENT";
        }
    }
    
    @UriTemplate("/")
    static public class ResourceWithSingleProduceMime { 
        @HttpMethod("GET")
        @ProduceMime("text/plain")
        public String doGet() {
            return "CONTENT";
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testReturnType() {
        HttpResponseContext response = callGet(Resource.class, "/", "");
        String r = (String)response.getEntity();

        assertEquals("CONTENT", r);
        assertEquals(new MediaType("application/octet-stream"), response.getHttpHeaders().getFirst("Content-Type"));
    }
    
    @SuppressWarnings("unchecked")
    public void testReturnHttpTypeWithSingleProduceMime() {
        HttpResponseContext response = callGet(ResourceWithSingleProduceMime.class, "/", "");
        String r = (String)response.getEntity();

        assertEquals("CONTENT", r);
        assertEquals(new MediaType("text/plain"), response.getHttpHeaders().getFirst("Content-Type"));
    }
}
