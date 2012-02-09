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
package com.sun.jersey.impl.container;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceConfigurator;
import com.sun.jersey.impl.AbstractResourceTester;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceConfiguratorTest extends AbstractResourceTester {

    public static abstract class StringHolder {
        public final String s;

        public StringHolder(String s) { this.s = s; }
    }

    public static class StringHolderOne extends StringHolder {
        public StringHolderOne(String s) { super(s); }
    }

    public static class StringHolderTwo extends StringHolder {
        public StringHolderTwo(String s) { super(s); }
    }
    
    @Produces("text/plain")
    public static class StringHolderOneWriter implements MessageBodyWriter<StringHolderOne> {

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return StringHolderOne.class == type;
        }

        public long getSize(StringHolderOne t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(StringHolderOne t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.s.toUpperCase().getBytes());
        }
    }

    @Produces("text/plain")
    public static class StringHolderTwoWriter implements MessageBodyWriter<StringHolderTwo> {

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return StringHolderTwo.class == type;
        }

        public long getSize(StringHolderTwo t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(StringHolderTwo t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.s.toUpperCase().getBytes());
        }
    }

    @Path("/one")
    public static class ResourceOne {
        @GET
        public StringHolderOne get() {
            return new StringHolderOne("one");
        }
    }
    
    @Path("/two")
    public static class ResourceTwo {
        @GET
        public StringHolderTwo get() {
            return new StringHolderTwo("two");
        }
    }

    public static class ConfigOne implements ResourceConfigurator {
        public void configure(ResourceConfig config) {
            config.getClasses().add(ResourceOne.class);
            config.getClasses().add(StringHolderOneWriter.class);
        }
    }

    public static class ConfigTwo implements ResourceConfigurator {
        public void configure(ResourceConfig config) {
            config.getClasses().add(ResourceTwo.class);

            config.getSingletons().add(new StringHolderTwoWriter());
        }
    }

    public ResourceConfiguratorTest(String testName) {
        super(testName);
    }
        

    public void testWithString() {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getClasses().add(ConfigOne.class);
        rc.getClasses().add(ConfigTwo.class);

        initiateWebApplication(rc);

        WebResource r = resource("/");
        
        assertEquals("ONE", r.path("one").get(String.class));
        assertEquals("TWO", r.path("two").get(String.class));
    }

    public void testCommonDelimiterLineBreak() {
        ResourceConfig rc = new ClassNamesResourceConfig(ConfigOne.class.getName() + "\n" + ConfigTwo.class.getName());

        initiateWebApplication(rc);

        WebResource r = resource("/");

        assertEquals("ONE", r.path("one").get(String.class));
        assertEquals("TWO", r.path("two").get(String.class));
    }

}
