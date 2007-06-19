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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.ws.rest.impl.HttpRequestContextImpl;
import com.sun.ws.rest.impl.http.header.HttpHeaderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.MimeHeaders;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class GrizzlyRequestAdaptor  extends HttpRequestContextImpl {
    
    private Request request;
    
    /** Creates a new instance of GrizzlyRequestAdaptor */
    public GrizzlyRequestAdaptor(Request request) {
        super(request.method().toString(), new GrizzlyRequestInputStream(request));
        this.request = request;
        this.uriPath = request.requestURI().toString();
        // Ensure path is relative, TODO may need to check for multiple '/'
        if (this.uriPath.startsWith("/"))
            this.uriPath = this.uriPath.substring(1);
        
        extractQueryParameters(request.queryString().toString());
        try {
            String scheme = request.scheme().toString();
            String host = request.serverName().toString();
            this.baseURI = new URI(scheme, host, null);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        copyHttpHeaders();
    }
    
    protected void copyHttpHeaders() {
        MultivaluedMap<String, String> headers = getRequestHeaders();
        
        MimeHeaders mh = request.getMimeHeaders();
        Enumeration names = mh.names();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            String value = mh.getHeader(name);
            headers.add(name, value);
            if (name.equalsIgnoreCase("cookie")) {
                getCookies().addAll(HttpHeaderFactory.createCookies(value));
            }
        }
    }

    
    private static class GrizzlyRequestInputStream extends InputStream {
        
        Request request;
        ByteArrayInputStream stream;
        ByteChunk chunk;
        
        public GrizzlyRequestInputStream(Request request) {
            this.request = request;
            this.stream = null;
            this.chunk = new ByteChunk();
        }

        public int read() throws IOException {
            refillIfRequired();
            return stream.read();
        }
        
        public int read(byte[] b) throws IOException {
            refillIfRequired();
            return stream.read(b);
        }
        
        private void refillIfRequired() throws IOException {
            if (stream==null || stream.available()==0) {
                //chunk.recycle();
                request.doRead(chunk);
                if (chunk.getLength() > 0)
                    stream = new ByteArrayInputStream(chunk.getBytes(), chunk.getStart(), chunk.getLength());
            }
        }
    }
}
