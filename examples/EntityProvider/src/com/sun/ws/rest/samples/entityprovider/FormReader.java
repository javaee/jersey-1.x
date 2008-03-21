/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * FormReader.java
 *
 * Created on November 21, 2007, 12:43 PM
 *
 */

package com.sun.ws.rest.samples.entityprovider;

import com.sun.ws.rest.samples.entityprovider.resources.NameValuePair;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author mh124079
 */
@ConsumeMime("application/x-www-form-urlencoded")
@Provider
public class FormReader implements MessageBodyReader<NameValuePair> {
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations) {
        return type.equals(NameValuePair.class);
    }

    public NameValuePair readFrom(Class<NameValuePair> type, Type genericType,
            MediaType mediaType, Annotation[] annotations, MultivaluedMap<String, String> headers, 
            InputStream in) throws IOException {
        String formData = readAsString(in);

        Map<String, String> map = new HashMap<String, String>();
        StringTokenizer tokenizer = new StringTokenizer(formData, "&");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            int idx = token.indexOf('=');
            if (idx < 0) {
                map.put(URLDecoder.decode(token,"UTF-8"), null);
            } else if (idx > 0) {
                map.put(URLDecoder.decode(token.substring(0, idx),"UTF-8"), URLDecoder.decode(token.substring(idx+1),"UTF-8"));
            }
        }
        
        return new NameValuePair(map.get("name"), map.get("value"));
    }
    
    public final String readAsString(InputStream in) throws IOException {
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
