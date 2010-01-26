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

package com.sun.jersey.impl.template;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TemplateProcessorTest extends AbstractResourceTester {
    
    public TemplateProcessorTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ExplicitTemplate {
        @GET public Viewable get() {
            return new Viewable("show", "get");
        }

        @POST public Viewable post() {
            return new Viewable("show", "post");
        }

        @Path("absolute")
        @GET public Viewable getAbs() {
            return new Viewable("/com/sun/jersey/impl/template/TemplateProcessorTest/ExplicitTemplate/absolute/show", "get");
        }

        @Path("absolute")
        @POST public Viewable postAbs() {
            return new Viewable("/com/sun/jersey/impl/template/TemplateProcessorTest/ExplicitTemplate/absolute/show", "post");
        }
    }

    public void testExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ExplicitTemplate.class,
                TestTemplateProcessor.class);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        p.load(r.get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(r.post(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));
    }

    public void testExplicitAbsoluteTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ExplicitTemplate.class,
                TestTemplateProcessor.class);
        initiateWebApplication(rc);
        WebResource r = resource("/absolute");

        Properties p = new Properties();
        p.load(r.get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ExplicitTemplate/absolute/show.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(r.post(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ExplicitTemplate/absolute/show.testp", p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));
    }

    @Path("/")
    public static class ImplicitTemplate {
        public String toString() {
            return "ImplicitTemplate";
        }
    }

    public void testImplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitTemplate.class,
                TestTemplateProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        p.load(r.get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ImplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }

    @Path("/")
    public static class ImplicitExplicitTemplate {
        public String toString() {
            return "ImplicitExplicitTemplate";
        }

        @POST public Viewable post() {
            return new Viewable("show", "post");
        }

        @Path("sub") @GET public Viewable get() {
            return new Viewable("show", "get");
        }
    }

    public void testImplicitExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitExplicitTemplate.class,
                TestTemplateProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        p.load(r.get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ImplicitExplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitExplicitTemplate", p.getProperty("model"));

        p = new Properties();
        p.load(r.post(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ImplicitExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));

        p = new Properties();
        p.load(r.path("sub").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ImplicitExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));
    }

    @Path("/")
    public static class ImplicitWithGetTemplate {
        @GET
        @Produces("application/foo")
        public String toString() {
            return "ImplicitWithGetTemplate";
        }
    }

    public void testImplicitWithGetTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitWithGetTemplate.class,
                TestTemplateProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        p.load(r.accept("text/plain").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ImplicitWithGetTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitWithGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithGetTemplate", r.accept("application/foo").get(String.class));
    }

    @Path("/")
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
                TestTemplateProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        initiateWebApplication(rc);
        WebResource r = resource("/sub");

        Properties p = new Properties();
        p.load(r.accept("text/plain").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/TemplateProcessorTest/ImplicitWithSubResourceGetTemplate/sub.testp", p.getProperty("path"));
        assertEquals("ImplicitWithSubResourceGetTemplate", p.getProperty("model"));

        assertEquals("ImplicitWithSubResourceGetTemplate", r.accept("application/foo").get(String.class));
    }

}