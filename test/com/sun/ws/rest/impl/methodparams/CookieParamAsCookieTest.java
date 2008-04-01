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

import com.sun.ws.rest.api.client.WebResource;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.util.Map;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class CookieParamAsCookieTest extends AbstractResourceTester {

    public CookieParamAsCookieTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class CookieTypeResource {
        @POST public void post(
                @Context HttpHeaders h,
                @CookieParam("one") Cookie one,
                @CookieParam("two") Cookie two,
                @CookieParam("three") Cookie three) {
            assertEquals("one", one.getName());
            assertEquals("value_one", one.getValue());
            
            assertEquals("two", two.getName());
            assertEquals("value_two", two.getValue());

            assertEquals(null, three);
            
            Map<String, Cookie> cs = h.getCookies();
            assertEquals(2, cs.size());
            assertEquals("value_one", cs.get("one").getValue());
            assertEquals("value_two", cs.get("two").getValue());
        }
    }

    public void testCookieParam() {
        initiateWebApplication(CookieTypeResource.class);
        
        WebResource r = resource("/");
        
        Cookie one = new Cookie("one", "value_one");
        Cookie two = new Cookie("two", "value_two");
        r.cookie(one).cookie(two).post();
    }
}