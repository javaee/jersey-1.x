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

package com.sun.jersey.impl.entity;

import com.sun.jersey.impl.AbstractResourceTester;
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
        
        public boolean isReadable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
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
    public static class ListResource {
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