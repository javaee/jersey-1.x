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

package com.sun.jersey.osgi.tests.jetty;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class ApplicationTest extends AbstractJettyWebContainerTester {
    
    public static class App extends Application {

        private Set<Class<?>> classes = new HashSet<Class<?>>();

        public App() {
            classes.add(Resource.class);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return classes;
        }

    }

    public static class ResourceConfigApp extends DefaultResourceConfig {
        public ResourceConfigApp() {
            getClasses().add(Resource.class);
        }
    }

    @Path("/")
    public static class Resource {

        @GET
        @Produces("text/plain")
        public String get(@Context ResourceConfig rc) {
            assertTrue(rc.getFeature("feature"));
            return rc.getProperty("property").toString();
        }
    }

    public void testAppWithResourceConfigPropertyName() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, App.class.getName());
        initParams.put("property", "test");
        initParams.put("feature", "true");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("test", r.get(String.class));
    }

    @Test
    public void testAppWithApplicationPropertyName() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("javax.ws.rs.Application", App.class.getName());
        initParams.put("property", "test");
        initParams.put("feature", "true");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("test", r.get(String.class));
    }


    @Test
    public void testResourceConfgiWithResourceConfigPropertyName() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, ResourceConfigApp.class.getName());
        initParams.put("property", "test");
        initParams.put("feature", "true");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("test", r.get(String.class));
    }

    @Test
    public void testResourceConfgiWithApplicationPropertyName() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("javax.ws.rs.Application", ResourceConfigApp.class.getName());
        initParams.put("property", "test");
        initParams.put("feature", "true");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("test", r.get(String.class));
    }


    public static class OverridePropertyApp extends DefaultResourceConfig {
        public OverridePropertyApp() {
            getClasses().add(ResourceOverride.class);
            getSingletons().add(new ResourceSingleton());
            getExplicitRootResources().put("/explicit", new ResourceSingleton());

            getMediaTypeMappings().put("xml", MediaType.APPLICATION_XML_TYPE);
            getLanguageMappings().put("en", "en");

            getProperties().put("property", "override");
            getFeatures().put("overridefeature", false);
        }
    }

    @Path("/singleton")
    public static class ResourceSingleton {
        @GET
        public String get() {
            return "GET";
        }
    }

    @Path("/")
    public static class ResourceOverride {

        @GET
        @Produces("text/plain")
        public String get(@Context ResourceConfig rc) {

            assertEquals(ResourceSingleton.class,
                    rc.getSingletons().iterator().next().getClass());
            assertEquals(ResourceSingleton.class,
                    rc.getExplicitRootResources().get("/explicit").getClass());

            assertEquals(MediaType.APPLICATION_XML_TYPE,
                    rc.getMediaTypeMappings().get("xml"));
            assertEquals("en",
                    rc.getLanguageMappings().get("en"));

            assertFalse(rc.getFeature("overridefeature"));
            assertTrue(rc.getFeature("feature"));
            return rc.getProperty("property").toString();
        }
    }

    @Test
    public void testOverrideProperty() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, OverridePropertyApp.class.getName());
        initParams.put("property", "test");
        initParams.put("feature", "true");
        initParams.put("overridefeature", "true");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("override", r.get(String.class));
    }

    public static class InjectApp extends DefaultResourceConfig {
        public InjectApp(@Context ServletContext sc) {
            getClasses().add(ResourceInjectApp.class);

            assertNotNull(sc);
            getProperties().put("z", sc.getInitParameter("x"));
        }
    }

    @Path("/")
    public static class ResourceInjectApp {

        @GET
        @Produces("text/plain")
        public String get(@Context ResourceConfig rc) {
            return rc.getProperty("z").toString();
        }
    }

    @Test
    public void testInject() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, InjectApp.class.getName());
        initParams.put("x", "y");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("y", r.get(String.class));
    }


    public static class SingletonTypeOne {
    }

    public static class SingletonTypeTwo {
    }

    public class SingletonTypeProvider extends SingletonTypeInjectableProvider<Context, SingletonTypeOne> {
        public SingletonTypeProvider() {
            super(SingletonTypeOne.class, new SingletonTypeOne());
        }
    }

    @Produces("text/plain")
    public class ToUpperWriter implements MessageBodyWriter<String> {

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return String.class == type;
        }

        public long getSize(String t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(String t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.toUpperCase().getBytes());
        }
    }

    public static class InjectAppProvider extends DefaultResourceConfig {
        public InjectAppProvider(@Context ServletContext sc) {
            getClasses().add(ResourceInjectAppProvider.class);
            getClasses().add(SingletonTypeProvider.class);
            getClasses().add(ToUpperWriter.class);
            
            getSingletons().add(
                    new SingletonTypeInjectableProvider<Context, SingletonTypeTwo>(
                        SingletonTypeTwo.class, new SingletonTypeTwo()) {});

            assertNotNull(sc);
            getProperties().put("z", sc.getInitParameter("x"));
        }
    }

    @Path("/")
    public static class ResourceInjectAppProvider {

        public ResourceInjectAppProvider(@Context SingletonTypeOne one, @Context SingletonTypeTwo two) {
            assertNotNull(one);
            assertNotNull(two);
        }

        @GET
        @Produces("text/plain")
        public String get(@Context ResourceConfig rc) {
            return rc.getProperty("z").toString();
        }
    }

    @Test
    public void testInjectProvider() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ServletContainer.RESOURCE_CONFIG_CLASS, InjectAppProvider.class.getName());
        initParams.put("x", "y");

        startServer(initParams);

        WebResource r = Client.create().resource(getUri().
                path("/").build());

        assertEquals("Y", r.get(String.class));
    }
}