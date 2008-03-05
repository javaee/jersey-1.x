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

package com.sun.ws.rest.impl.template;

import com.sun.ws.rest.api.client.WebResource;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.view.Viewable;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
    }
    
    public void testExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ExplicitTemplate.class);
        rc.getProviderClasses().add(TestTemplateProcessor.class);        
        initiateWebApplication(rc);
        WebResource r = resource("/");
        
        Properties p = new Properties();
        p.load(r.get(InputStream.class));        
        assertEquals("/com/sun/ws/rest/impl/template/TemplateProcessorTest/ExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));
        
        p = new Properties();
        p.load(r.post(InputStream.class));        
        assertEquals("/com/sun/ws/rest/impl/template/TemplateProcessorTest/ExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));        
    }
    
    @Path("/")
    public static class ImplicitTemplate {
        public String toString() {
            return "ImplicitTemplate";
        } 
    }
    
    public void testImplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitTemplate.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        rc.getProviderClasses().add(TestTemplateProcessor.class);        
        initiateWebApplication(rc);
        WebResource r = resource("/");
        
        Properties p = new Properties();
        p.load(r.get(InputStream.class));        
        assertEquals("/com/sun/ws/rest/impl/template/TemplateProcessorTest/ImplicitTemplate/index.testp", p.getProperty("path"));
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
        ResourceConfig rc = new DefaultResourceConfig(ImplicitExplicitTemplate.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        rc.getProviderClasses().add(TestTemplateProcessor.class);        
        initiateWebApplication(rc);
        WebResource r = resource("/");
        
        Properties p = new Properties();
        p.load(r.get(InputStream.class));        
        assertEquals("/com/sun/ws/rest/impl/template/TemplateProcessorTest/ImplicitExplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitExplicitTemplate", p.getProperty("model"));
        
        p = new Properties();
        p.load(r.post(InputStream.class));        
        assertEquals("/com/sun/ws/rest/impl/template/TemplateProcessorTest/ImplicitExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("post", p.getProperty("model"));        
        
        p = new Properties();
        p.load(r.path("sub").get(InputStream.class));        
        assertEquals("/com/sun/ws/rest/impl/template/TemplateProcessorTest/ImplicitExplicitTemplate/show.testp", p.getProperty("path"));
        assertEquals("get", p.getProperty("model"));        
    }
    
}