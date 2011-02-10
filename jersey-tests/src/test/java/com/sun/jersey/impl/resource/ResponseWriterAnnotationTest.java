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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResponseWriterAnnotationTest extends AbstractResourceTester {
    @Provider
    @Produces("text/plain")
    public static class StringWriter implements MessageBodyWriter<String> {

        public boolean isWriteable(Class<?> c, Type t, Annotation[] as, MediaType mediaType) {
            assertTrue(findAnnotation(EntityAnnotation.class, as));
            return String.class == c;
        }

        public long getSize(String s, Class<?> type, Type genericType, Annotation[] as,
                MediaType mediaType) {
            assertTrue(findAnnotation(EntityAnnotation.class, as));
            return -1;
        }

        public void writeTo(String s, Class<?> c, Type t, Annotation[] as, 
                MediaType mt, MultivaluedMap<String, Object> headers, OutputStream out) 
                throws IOException, WebApplicationException {
            assertTrue(findAnnotation(EntityAnnotation.class, as));
            out.write(s.getBytes());
        }

        private boolean findAnnotation(Class<? extends Annotation> ac, Annotation[] as) {
            for (Annotation a : as) {
                if (a.annotationType() == ac) {
                    return true;
                }
            }
            return false;
        }
    }
    
    
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface EntityAnnotation {}

    @Path("/")
    public static class Resource {
        @EntityAnnotation
        @GET
        public String get() {
            return "GET";
        }               
    }
        
    public ResponseWriterAnnotationTest(String testName) {
        super(testName);
    }
    
    public void testResponse() {
        initiateWebApplication(Resource.class, StringWriter.class);
        
        assertEquals("GET", resource("/").get(String.class));
    }
}