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
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class InheritanceTest extends AbstractBeanTester {
    
    public InheritanceTest(String testName) {
        super(testName);
    }

    static public abstract class SuperResource { 
        @HttpMethod("GET")
        @ProduceMime("application/super")
        public String doGet() {
            return "super";
        }
    }
    
    @UriTemplate("/")
    static public class SubResource extends SuperResource { 
        @HttpMethod("GET")
        @ProduceMime("application/sub")
        public String doGetSub() {
            return "sub";
        }
    }

    @UriTemplate("/")
    static public class SubResourceOverride extends SuperResource { 
        @HttpMethod("GET")
        @ProduceMime("application/sub")
        public String doGet() {
            return "suboverride";
        }
    }

    public void testSuperResourceGet() {
        HttpResponseContext response = callGet(SubResource.class, "/", "application/super");
        String sr = (String)response.getEntity();
        assertEquals("super", sr);
    }
    
    public void testSubResourceGet() {
        HttpResponseContext response = callGet(SubResource.class, "/", "application/sub");
        String sr = (String)response.getEntity();
        assertEquals("sub", sr);
    }
    
    public void testSuperResourceOverrideGet() {
        HttpResponseContext response = callNoStatusCheck(SubResourceOverride.class, "GET", 
                "/", null, "application/super", "");
        assertEquals(406, response.getStatus());
    }
    
    public void testSubResourceOverrideGet() {
        HttpResponseContext response = callGet(SubResourceOverride.class, "/", "application/sub");
        String sr = (String)response.getEntity();
        assertEquals("suboverride", sr);
    }

    
    @ProduceMime("application/default")
    static public abstract class SuperResourceWithProduce { 
        @HttpMethod("GET")
        @ProduceMime("application/super")
        public String doGet() {
            return "super";
        }
    }
    
    @UriTemplate("/")
    static public class SubResourceWithProduce extends SuperResourceWithProduce { 
        @HttpMethod("GET")
        public String doGetSub() {
            return "sub";
        }
    }
        
    @UriTemplate("/")
    static public class SubResourceWithProduceOverride extends SuperResourceWithProduce { 
        @HttpMethod("GET")
        public String doGet() {
            return "suboverride";
        }
    }
    
    public void testSuperResourceWithProduceGet() {
        HttpResponseContext response = callGet(SubResourceWithProduce.class, "/", "application/super");
        String sr = (String)response.getEntity();
        assertEquals("super", sr);
    }
    
    public void testSubResourceWithProduceGet() {
        HttpResponseContext response = callGet(SubResourceWithProduce.class, "/", "application/default");
        String sr = (String)response.getEntity();
        assertEquals("sub", sr);
    }
    
    public void testSuperResourceOverrideWithProduceGet() {
        HttpResponseContext response = callNoStatusCheck(SubResourceWithProduceOverride.class, "GET", 
                "/", null, "application/super", "");
        assertEquals(406, response.getStatus());
    }
    
    public void testSubResourceOverrideWithProduceGet() {
        HttpResponseContext response = callGet(SubResourceWithProduceOverride.class, "/", "application/default");
        String sr = (String)response.getEntity();
        assertEquals("suboverride", sr);
    }
    
    
    @UriTemplate("/")
    static public abstract class SuperResourceWithTemplate { 
        @HttpMethod("GET")
        @ProduceMime("application/super")
        public String doGet() {
            return "super";
        }
    }
    
    static public class SubResourceWithTemplate extends SuperResourceWithTemplate { 
        @HttpMethod("GET")
        public Response doGetSub() {
            return Response.Builder.representation("sub", "application/default").build();
        }
    }
    
    @UriTemplate("/override")
    static public class SubResourceWithTemplateOverride extends SuperResourceWithTemplate { 
        @HttpMethod("GET")
        public Response doGetSub() {
            return Response.Builder.representation("sub", "application/default").build();
        }
    }
    
    public void testSuperResourceWithTemplateGet() {
        HttpResponseContext response = callGet(SubResourceWithTemplate.class, "/", "application/super");
        String sr = (String)response.getEntity();
        assertEquals("super", sr);
    }
    
    public void testSubResourceWithTemplateGet() {
        HttpResponseContext response = callGet(SubResourceWithTemplate.class, "/", "application/sub");
        String sr = (String)response.getEntity();
        assertEquals("sub", sr);
    }
    
    public void testSuperResourceWithTemplateOverrideGet() {
        HttpResponseContext response = callNoStatusCheck(SubResourceWithTemplateOverride.class, "GET", 
                "/", null, "application/super", "");
        assertEquals(404, response.getStatus());
    }
    
    public void testSubResourceWithTemplateOverrideGet() {
        HttpResponseContext response = callGet(SubResourceWithTemplateOverride.class, "/override", "application/sub");
        String sr = (String)response.getEntity();
        assertEquals("sub", sr);
    }
}