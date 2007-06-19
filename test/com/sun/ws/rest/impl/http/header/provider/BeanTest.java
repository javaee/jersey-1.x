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

package com.sun.ws.rest.impl.http.header.provider;

import com.sun.ws.rest.impl.HttpResponseContextImpl;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import com.sun.ws.rest.impl.bean.AbstractBeanTester;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class BeanTest extends AbstractBeanTester {
    
    public BeanTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class TestResource {
        @HttpMethod("GET")
        public Response doGet() {
            GregorianCalendar lastModified = new GregorianCalendar(2007, 0, 0, 0, 0, 0);
            return Response.Builder.ok().
                    lastModified(lastModified.getTime()).
                    tag(new EntityTag("TAG")).
                    location(URI.create("/")).
                    language("en").build();
        }
    }
    
    public void testHeaders() {
        MultivaluedMap<String, String> h = new RequestHttpHeadersImpl();
        HttpResponseContextImpl r = callNoStatusCheck(TestResource.class, "GET", "/", h, "");
        
        MultivaluedMap<String, Object> headers = r.getHttpHeaders();
        Object value;
        String stringValue;
        value = headers.getFirst("Last-Modified");
        stringValue = r.getHeaderValue(value);
        assertEquals(ProviderFactory.newInstance().
                createHeaderProvider(Date.class).
                toString(new GregorianCalendar(2007, 0, 0, 0, 0, 0).getTime()),
                stringValue);
        
        value = headers.getFirst("ETag");
        stringValue = r.getHeaderValue(value);
        assertEquals(ProviderFactory.newInstance().
                createHeaderProvider(EntityTag.class).
                toString(new EntityTag("TAG")),
                stringValue);
        
        value = headers.getFirst("Location");
        stringValue = r.getHeaderValue(value);
        assertEquals(ProviderFactory.newInstance().
                createHeaderProvider(URI.class).
                toString(URI.create("/")),
                stringValue);
        
        value = headers.getFirst("Content-Language");
        stringValue = r.getHeaderValue(value);
        assertEquals("en", stringValue);
    }
}
