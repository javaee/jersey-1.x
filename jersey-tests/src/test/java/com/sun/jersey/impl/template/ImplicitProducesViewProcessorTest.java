/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import com.sun.jersey.api.view.ImplicitProduces;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ImplicitProducesViewProcessorTest extends AbstractResourceTester {
    
    public ImplicitProducesViewProcessorTest(String testName) {
        super(testName);
    }

    @Path("/")
    @ImplicitProduces("text/plain;qs=5")
    public static class ImplicitTemplate {
        public String toString() {
            return "ImplicitTemplate";
        }
    }

    public void testImplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitTemplate.class,
                TestViewProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        ClientResponse cr = r.accept("text/plain", "application/foo").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("application/foo", "text/plain").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("text/plain;q=0.5", "application/foo").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("application/foo", "text/plain;q=0.5").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }

    @Path("/")
    @ImplicitProduces("text/plain;qs=5")
    public static class ImplicitWithGetTemplate {
        @GET
        @Produces("application/foo")
        public String toString() {
            return "ImplicitWithGetTemplate";
        }
    }

    public void testImplicitWithGetTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitWithGetTemplate.class,
                TestViewProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        ClientResponse cr = r.accept("text/plain", "application/foo").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("application/foo", "text/plain").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("text/plain;q=0.5", "application/foo").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("application/foo", "text/plain;q=0.5").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("*/*").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithGetTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithGetTemplate", r.accept("application/foo").get(String.class));

    }

    @Path("/")
    @ImplicitProduces("text/plain;qs=5")
    public static class ImplicitWithSubResourceGetTemplate {
        @Path("sub")
        @GET
        @Produces("application/foo")
        public String toString() {
            return "ImplicitWithSubResourceGetTemplate";
        }
    }

    public void testImplicitWithSubResourceGetTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitWithSubResourceGetTemplate.class,
                TestViewProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/sub");

        Properties p = new Properties();
        ClientResponse cr = r.accept("text/plain", "application/foo").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithSubResourceGetTemplate/sub.testp", p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("application/foo", "text/plain").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithSubResourceGetTemplate/sub.testp", p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("text/plain;q=0.5", "application/foo").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithSubResourceGetTemplate/sub.testp", p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        p = new Properties();
        cr = r.accept("application/foo", "text/plain;q=0.5").get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType()));
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitProducesViewProcessorTest/ImplicitWithSubResourceGetTemplate/sub.testp", p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithSubResourceGetTemplate", r.accept("application/foo").get(String.class));
    }

}