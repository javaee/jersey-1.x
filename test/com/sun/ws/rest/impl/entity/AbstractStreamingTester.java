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

import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import com.sun.ws.rest.impl.TestHttpRequestContext;
import com.sun.ws.rest.impl.TestHttpResponseContext;
import com.sun.ws.rest.impl.application.MessageBodyFactory;
import com.sun.ws.rest.spi.container.MessageBodyContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractStreamingTester extends TestCase {
    
    MessageBodyContext bodyContext;
    
    public AbstractStreamingTester(String testName) {
        super(testName);
        bodyContext = new MessageBodyFactory();
    }
    
    <T> void roundTrip(Class<T> c, T t) throws IOException {
        roundTrip(c, t, "text/plain");
    }
    
    <T> void roundTrip(Class<T> c, T t1, String mediaType) throws IOException {
        MediaType mt = MediaType.parse(mediaType);
        byte[] b1 = writeTo(t1, mt);
        T t2 = readFrom(c, b1, mt);
        byte[] b2 = writeTo(t2, mt);
        assertEquals(b1.length, b2.length);
        boolean e = false;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i])
                assertEquals("Index: " + i, b1[i], b2[i]);
        }
    }
    
    <T> T readFrom(Class<T> c, byte[] b) throws IOException {
        return readFrom(c, b, new MediaType("text", "plain"));
    }
    
    <T> T readFrom(Class<T> c, byte[] b, MediaType mediaType) throws IOException {
        RequestHttpHeadersImpl h = new RequestHttpHeadersImpl();
        h.add("Content-Type", mediaType);

        ByteArrayInputStream in = new ByteArrayInputStream(b);
        MessageBodyReader<T> tsp = bodyContext.getMessageBodyReader(c, mediaType);
        return tsp.readFrom(c, mediaType, h, in);
    }
    
    <T> byte[] writeTo(T t) throws IOException {
        return writeTo(t, new MediaType("text", "plain"));
    }
    
    @SuppressWarnings("unchecked")
    <T> byte[] writeTo(T t, MediaType mediaType) throws IOException {
        
        TestHttpRequestContext reqc = new TestHttpRequestContext(null);
        TestHttpResponseContext resc = new TestHttpResponseContext(null, reqc);
        
        Response r = Response.ok().type(mediaType).build();
        resc.setResponse(r);
        
        MessageBodyWriter<T> tsp = bodyContext.getMessageBodyWriter(
                (Class<T>)t.getClass(), mediaType);
        tsp.writeTo(t, mediaType, resc.getHttpHeaders(), resc.getOutputStream());
        return resc.getEntityAsByteArray();
    }
}
