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

import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ParameterizedTypeVariableTest extends AbstractResourceTester {
    
    public ParameterizedTypeVariableTest(String testName) {
        super(testName);
    }
    
    @Provider
    public static class ListStringReader implements MessageBodyReader<List<String>>,
            MessageBodyWriter<List<String>>{
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
            return Arrays.asList(ReaderWriter.readFromAsString(in, mt).split(","));
        }
        
        @Override
        public boolean isWriteable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
            return List.class.isAssignableFrom(c) && listStringType.equals(t);
        }

        @Override
        public long getSize(List<String> t, Class<?> type, Type genericType, Annotation[] as, MediaType mt) {
            return -1;
        }

        @Override
        public void writeTo(List<String> t, Class<?> c, Type genericType, Annotation[] as,
                MediaType mt, MultivaluedMap<String, Object> hs, OutputStream out) throws IOException, WebApplicationException {
            StringBuilder sb = new StringBuilder();
            for (String s : t) {
                if (sb.length() > 0) sb.append(',');
                sb.append(s);
            }
            out.write(sb.toString().getBytes());
        }
    }

    public static class GenericListResource<T> {
        @POST public List<T> post(List<T> ls) {
            return ls;
        }
    }

    @Path("/")
    public static class ListResource extends GenericListResource<String> {
    }
    
    public void testGenericList() throws Exception {
        initiateWebApplication(ListResource.class, ListStringReader.class);
        
        assertEquals("a,b,c,d", resource("/").post(String.class, "a,b,c,d"));
    }


    @Provider
    public static class MapStringReader implements MessageBodyReader<Map<String, String>>,
            MessageBodyWriter<Map<String, String>>{
        private final Type mapStringType;

        public MapStringReader() {
            ParameterizedType iface = (ParameterizedType)this.getClass().getGenericInterfaces()[0];
            mapStringType = iface.getActualTypeArguments()[0];
        }

        public boolean isReadable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
            return Map.class == c && mapStringType.equals(t);
        }

        public Map<String, String> readFrom(
                Class<Map<String, String>> c,
                Type t,
                Annotation[] as,
                MediaType mt,
                MultivaluedMap<String, String> headers, InputStream in) throws IOException {
            String[] v = ReaderWriter.readFromAsString(in, mt).split(",");
            Map<String, String> m = new LinkedHashMap<String, String>();
            for (int i = 0; i < v.length; i = i + 2) {
                m.put(v[i], v[i + 1]);
            }
            return m;
        }

        @Override
        public boolean isWriteable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
            return Map.class.isAssignableFrom(c) && mapStringType.equals(t);
        }

        @Override
        public long getSize(Map<String, String> t, Class<?> type, Type genericType, Annotation[] as, MediaType mt) {
            return -1;
        }

        @Override
        public void writeTo(Map<String, String> t, Class<?> c, Type genericType, Annotation[] as,
                MediaType mt, MultivaluedMap<String, Object> hs, OutputStream out) throws IOException, WebApplicationException {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : t.entrySet()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(e.getKey()).append(',').append(e.getValue());
            }
            out.write(sb.toString().getBytes());
        }
    }


    public static class GenericMapResource<K, V> {
        @POST public Map<K, V> post(Map<K, V> m) {
            return m;
        }
    }

    @Path("/")
    public static class MapResource extends GenericMapResource<String, String> {
    }

    public void testGenericMap() throws Exception {
        initiateWebApplication(MapResource.class, MapStringReader.class);

        assertEquals("a,b,c,d", resource("/").post(String.class, "a,b,c,d"));
    }




    @Provider
    public static class MapListStringReader implements MessageBodyReader<Map<String, List<String>>>,
            MessageBodyWriter<Map<String, List<String>>>{
        private final Type mapListStringType;

        public MapListStringReader() {
            ParameterizedType iface = (ParameterizedType)this.getClass().getGenericInterfaces()[0];
            mapListStringType = iface.getActualTypeArguments()[0];
        }

        public boolean isReadable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
            return Map.class == c && mapListStringType.equals(t);
        }

        public Map<String, List<String>> readFrom(
                Class<Map<String, List<String>>> c,
                Type t,
                Annotation[] as,
                MediaType mt,
                MultivaluedMap<String, String> headers, InputStream in) throws IOException {
            try {
                JSONObject o = new JSONObject(ReaderWriter.readFromAsString(in, mt));

                Map<String, List<String>> m = new LinkedHashMap<String, List<String>>();
                Iterator keys = o.keys();
                while (keys.hasNext()) {
                    String key = (String)keys.next();
                    List<String> l = new ArrayList<String>();
                    m.put(key, l);
                    JSONArray a = o.getJSONArray(key);
                    for (int i = 0; i < a.length(); i++) {
                        l.add(a.getString(i));
                    }
                }
                return m;
            } catch (JSONException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public boolean isWriteable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
            return Map.class.isAssignableFrom(c) && mapListStringType.equals(t);
        }

        @Override
        public long getSize(Map<String, List<String>> t, Class<?> type, Type genericType, Annotation[] as, MediaType mt) {
            return -1;
        }

        @Override
        public void writeTo(Map<String, List<String>> t, Class<?> c, Type genericType, Annotation[] as,
                MediaType mt, MultivaluedMap<String, Object> hs, OutputStream out) throws IOException, WebApplicationException {
            try {
                JSONObject o = new JSONObject();
                for (Map.Entry<String, List<String>> e : t.entrySet()) {
                    o.put(e.getKey(), e.getValue());
                }
                out.write(o.toString().getBytes());
            } catch (JSONException ex) {
                throw new IOException(ex);
            }
        }
    }

    public static class GenericMapListResource<K, V> {
        @POST public Map<K, List<V>> post(Map<K, List<V>> m) {
            return m;
        }
    }

    @Path("/")
    public static class MapListResource extends GenericMapListResource<String, String> {
    }

    public void testGenericMapList() throws Exception {
        initiateWebApplication(MapResource.class, MapStringReader.class);

        String json = "{\"a\" : [\"1\", \"2\"], \"b\" : [\"1\", \"2\"]}";
        assertEquals(json, resource("/").post(String.class, json));
    }

}