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

import com.sun.ws.rest.impl.HttpRequestContextImpl;
import com.sun.ws.rest.impl.HttpResponseContextImpl;
import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.TestHttpRequestContext;
import java.net.URI;
import javax.ws.rs.ext.EntityProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ProviderFactory;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractStreamingTester extends TestCase {
    
    public AbstractStreamingTester(String testName) {
        super(testName);
    }
    
    <T> void roundTrip(Class<T> c, T t) throws IOException {
        roundTrip(c, t, "text/plain");
    }
    
    <T> void roundTrip(Class<T> c, T t1, String mediaType) throws IOException {
        byte[] b1 = writeTo(t1);
        T t2 = readFrom(c, b1);
        byte[] b2 = writeTo(t2);
        assertEquals(b1.length, b2.length);
        boolean e = false;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i])
                assertEquals("Index: " + i, b1[i], b2[i]);
        }
    }
    
    <T> T readFrom(Class<T> c, byte[] b) throws IOException {
        return readFrom(c, b, "text/plain");
    }
    
    <T> T readFrom(Class<T> c, byte[] b, String mediaType) throws IOException {
        RequestHttpHeadersImpl h = new RequestHttpHeadersImpl();
        h.add("Content-Type", mediaType);

        ByteArrayInputStream in = new ByteArrayInputStream(b);
        EntityProvider<T> tsp = ProviderFactory.getInstance().createEntityProvider(c);
        return tsp.readFrom(c, mediaType, h, in);
    }
    
    <T> byte[] writeTo(T t) throws IOException {
        return writeTo(t, "text/plain");
    }
    
    @SuppressWarnings("unchecked")
    <T> byte[] writeTo(T t, String mediaType) throws IOException {
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpRequestContextImpl reqc = new TestHttpRequestContext();
        HttpResponseContextImpl resc = new HttpResponseContextImpl(reqc) {
            public OutputStream getOutputStream() throws IOException {
                return out;
            }
        };
        
        Response r = new ResponseBuilderImpl().type(mediaType).build();
        resc.setResponse(r);
        
        EntityProvider<T> tsp = ProviderFactory.getInstance().createEntityProvider((Class<T>)t.getClass());
        tsp.writeTo(t, resc.getHttpHeaders(), resc.getOutputStream());
        return out.toByteArray();
    }
}
