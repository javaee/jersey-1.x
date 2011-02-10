/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FlatInheritedViewProcessorTest extends AbstractResourceTester {
    
    public FlatInheritedViewProcessorTest(String testName) {
        super(testName);
    }

    public static class ExplicitTemplateBase {
    }

    @Path("/")
    public static class ExplicitTemplate extends ExplicitTemplateBase {
        @GET public Viewable get() {
            return new Viewable("show", "get");
        }

        @Path("inherit")
        @GET public Viewable getInherited() {
            return new Viewable("inherit", "get");
        }

        @Path("override")
        @GET public Viewable getOverriden() {
            return new Viewable("override", "get");
        }
    }

    public void testExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ExplicitTemplate.class,
                TestViewProcessor.class);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        Properties p = new Properties();
        p.load(r.get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/FlatInheritedViewProcessorTest.ExplicitTemplateBase.show.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(r.path("inherit").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/FlatInheritedViewProcessorTest.ExplicitTemplateBase.inherit.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));

        p = new Properties();
        p.load(r.path("override").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/FlatInheritedViewProcessorTest.ExplicitTemplate.override.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));
    }


    public static class ImplicitTemplateBase {
    }
    
    @Path("/")
    public static class ImplicitTemplate extends ImplicitTemplateBase {
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
        p.load(r.get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/FlatInheritedViewProcessorTest.ImplicitTemplateBase.index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        p.load(r.path("inherit").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/FlatInheritedViewProcessorTest.ImplicitTemplateBase.inherit.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));

        p = new Properties();
        p.load(r.path("override").get(InputStream.class));
        assertEquals("/com/sun/jersey/impl/template/FlatInheritedViewProcessorTest.ImplicitTemplate.override.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }

}