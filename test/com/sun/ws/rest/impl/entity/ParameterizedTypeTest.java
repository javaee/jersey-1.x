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

package com.sun.ws.rest.impl.entity;

import com.sun.ws.rest.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ParameterizedTypeTest extends AbstractResourceTester {
    
    public ParameterizedTypeTest(String testName) {
        super(testName);
    }
    
    @Provider
    public static class ListStringReader implements MessageBodyReader<List<String>> {
        private final Type listStringType;
        
        public ListStringReader() {
            ParameterizedType iface = (ParameterizedType)this.getClass().getGenericInterfaces()[0];
            listStringType = iface.getActualTypeArguments()[0];
        }
        
        public boolean isReadable(Class<?> c, Type t, Annotation[] as) {
            return List.class == c && listStringType.equals(t);
        }

        public List<String> readFrom(
                Class<List<String>> c, 
                Type t, 
                Annotation[] as, 
                MediaType mt, 
                MultivaluedMap<String, String> headers, InputStream in) throws IOException {
            return Arrays.asList(readFromAsString(in).split(","));            
        }
        
        private String readFromAsString(InputStream in) throws IOException {
            Reader reader = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            char[] c = new char[1024];
            int l;
            while ((l = reader.read(c)) != -1) {
                sb.append(c, 0, l);
            } 
            return sb.toString();
        }
    }
    
    @Path("/")
    public class ListResource {
        @POST public String post(List<String> ls) {
            assertEquals(4, ls.size());
            assertEquals(Arrays.asList("a", "b", "c", "d"), ls);
            
            String v = "";
            for (String s : ls) {
                if (v.length() > 0) v += ",";
                v += s;
            }
            return v;
        }
    }
    
    public void testBean() throws Exception {
        initiateWebApplication(ListResource.class, ListStringReader.class);
        
        assertEquals("a,b,c,d", resource("/").post(String.class, "a,b,c,d"));
    }   
}