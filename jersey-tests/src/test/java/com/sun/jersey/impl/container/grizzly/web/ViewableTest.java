/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.container.grizzly.web;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.TemplateProcessor;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ViewableTest extends AbstractGrizzlyWebContainerTester {
    public static class XTemplateProcessor implements TemplateProcessor {

        public String resolve(String name) {
            if (name.endsWith(".x"))
                return name;
            else
                return null;
        }

        public void writeTo(String fullyQualifedName, Object model, OutputStream out) throws IOException {
            out.flush();

            out.write(fullyQualifedName.getBytes());
        }

    }

    @Path("/")
    public static class ViewableResource {
        @GET
        public Viewable get() {
            return new Viewable("/view.x", this);
        }

        @GET
        @Path("500")
        public Response get500() {
            return Response.status(500).
                    header("X-FOO", "foo").
                    entity(new Viewable("/view.x", this)).
                    build();
        }
    }
        
    public ViewableTest(String testName) {
        super(testName);
    }
    
    protected Client createClient() {
        return Client.create();
    }

    public void testGet() {
        startServer(ViewableResource.class, XTemplateProcessor.class);
        WebResource r = createClient().resource(getUri().path("/").build());
        assertEquals("/view.x", r.get(String.class));
    }
    
    public void testGet500() {
        startServer(ViewableResource.class, XTemplateProcessor.class);
        WebResource r = createClient().resource(getUri().path("/500").build());

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(500, cr.getStatus());
        assertNotNull(cr.getHeaders().getFirst("X-FOO"));
        assertEquals("foo", cr.getHeaders().getFirst("X-FOO"));
        assertEquals("/view.x", cr.getEntity(String.class));
    }
}
