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

package com.sun.jersey.impl.template;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ExceptionViewProcessorTest extends AbstractResourceTester {
    
    public ExceptionViewProcessorTest(String testName) {
        super(testName);
    }

    @Provider
    public static class WebAppAbsoluteExceptionMapper implements ExceptionMapper<WebApplicationException> {

        public Response toResponse(WebApplicationException exception) {
            if (exception.getResponse().getStatus() == 404) {
                return Response.status(404).entity(
                        new Viewable("/com/sun/jersey/impl/template/ExceptionViewProcessorTest/404", "404")).build();
            }

            return exception.getResponse();
        }

    }

    @Provider
    public static class WebAppResolvingClassExceptionMapper implements ExceptionMapper<WebApplicationException> {

        public Response toResponse(WebApplicationException exception) {
            if (exception.getResponse().getStatus() == 404) {
                return Response.status(404).entity(
                        new Viewable("404", "404", WebAppResolvingClassExceptionMapper.class)).build();
            }

            return exception.getResponse();
        }

    }

    @Path("/")
    public static class ExplicitTemplate {
        @GET public Viewable get() {
            return new Viewable("show", "get");
        }
    }

    public void testAbsoluteExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ExplicitTemplate.class,
                TestViewProcessor.class, WebAppAbsoluteExceptionMapper.class);
        initiateWebApplication(rc);
        WebResource r = resource("/doesnotexist", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(404, cr.getStatus());

        Properties p = new Properties();
        p.load(cr.getEntity(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/ExceptionViewProcessorTest/404.testp", p.getProperty("path"));
        assertEquals("404", p.getProperty("model"));
    }

    public void testResolvingClassExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ExplicitTemplate.class,
                TestViewProcessor.class, WebAppResolvingClassExceptionMapper.class);
        initiateWebApplication(rc);
        WebResource r = resource("/doesnotexist", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(404, cr.getStatus());

        Properties p = new Properties();
        p.load(cr.getEntity(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/ExceptionViewProcessorTest/WebAppResolvingClassExceptionMapper/404.testp", p.getProperty("path"));
        assertEquals("404", p.getProperty("model"));
    }
}