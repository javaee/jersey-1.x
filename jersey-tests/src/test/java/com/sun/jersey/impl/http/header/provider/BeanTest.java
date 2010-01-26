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

package com.sun.jersey.impl.http.header.provider;

import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.ClientResponse;
import java.net.URI;
import java.util.GregorianCalendar;
import javax.ws.rs.GET;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class BeanTest extends AbstractResourceTester {
    
    public BeanTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class TestResource {
        @GET
        public Response doGet() {
            GregorianCalendar lastModified = new GregorianCalendar(2007, 0, 0, 0, 0, 0);
            return Response.ok().
                    lastModified(lastModified.getTime()).
                    tag(new EntityTag("TAG")).
                    location(URI.create("/location")).
                    language("en").build();
        }
    }
    
    public void testHeaders() {
        initiateWebApplication(TestResource.class);
        
        ClientResponse response = resource("/").get(ClientResponse.class);
        
        assertEquals(new GregorianCalendar(2007, 0, 0, 0, 0, 0).getTime(),
                response.getLastModified());
        
        assertEquals(new EntityTag("TAG"),
                response.getEntityTag());
        
        assertEquals(UriBuilder.fromUri(BASE_URI).path("location").build(),
                response.getLocation());

        assertEquals("en",
                response.getLanguage());
    }
}
