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

package com.sun.jersey.impl.methodparams;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.impl.AbstractResourceTester;
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