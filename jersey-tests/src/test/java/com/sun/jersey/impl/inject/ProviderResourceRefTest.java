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

package com.sun.jersey.impl.inject;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author Marc Hadley
 */
public class ProviderResourceRefTest extends AbstractResourceTester {
            
    public ProviderResourceRefTest(String testName) {
        super(testName);
    }

    @Path("/")
    @Singleton
    public static class SingletonResource {
        int i = 0;

        @GET
        public String get() {
            i++;
            return "GET";
        }
    }

    public static class StringWriterWithInject implements MessageBodyWriter<String> {
        @InjectParam SingletonResource sr;

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        public long getSize(String t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(String t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            String s = t + sr.i;
            entityStream.write(s.getBytes());
        }
    }

    
    public void testMessageBodyWriterProvider() {
        initiateWebApplication(StringWriterWithInject.class, SingletonResource.class);
        WebResource r = resource("/");
        String s = r.get(String.class);
        assertEquals("GET1", s);
    }


    public static class Filter implements ContainerResponseFilter {
        @InjectParam SingletonResource sr;
        
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            response.getHttpHeaders().putSingle("X-Filter", "" + sr.i);
            return response;
        }
    }

    public void testFilterProvider() {
        ResourceConfig rc = new DefaultResourceConfig(SingletonResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Filter.class.getName());
        initiateWebApplication(rc);

        WebResource r = resource("/");
        ClientResponse cr = r.get(ClientResponse.class);
        assertNotNull(cr.getHeaders().getFirst("X-Filter"));
        assertEquals("1", cr.getHeaders().getFirst("X-Filter"));
        assertEquals("GET", cr.getEntity(String.class));
    }

}