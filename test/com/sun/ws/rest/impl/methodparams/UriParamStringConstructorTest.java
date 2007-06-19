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
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.bean.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriParamStringConstructorTest extends AbstractBeanTester {

    public UriParamStringConstructorTest(String testName) {
        super(testName);
    }

    @UriTemplate("/{a}/{b}")
    public static class Resource {
        @HttpMethod("GET")
        public String doGet(
                @UriParam("a") BigDecimal a, 
                @UriParam("b") BigInteger b) {
            assertEquals("3.145", a.toString());
            assertEquals("3145", b.toString());
            return "content";
        }
    }
        
    public void testStringConstructorGet() {
        Class r = Resource.class;
        callGet(r, "/3.145/3145", 
                "text/plain");
    }
    
    public void testBadStringConstructorValue() {
        Class r = Resource.class;
        HttpResponseContext response = callNoStatusCheck(r, "GET",
                "/ABCDE/ABCDE", null, "text/plain", "");
        assertEquals(400, response.getResponse().getStatus());
    }
}
