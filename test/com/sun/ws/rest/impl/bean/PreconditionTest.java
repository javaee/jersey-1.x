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

package com.sun.ws.rest.impl.bean;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import java.util.GregorianCalendar;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PreconditionEvaluator;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class PreconditionTest extends AbstractBeanTester {
    
    public PreconditionTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class LastModifiedResource {
        @HttpContext PreconditionEvaluator evaluator;

        @HttpMethod("GET")
        public Response doGet() {
            GregorianCalendar lastModified = new GregorianCalendar(2007, 0, 0, 0, 0, 0);
            Response r = evaluator.evaluate(lastModified.getTime());
            if (r != null)
                return r;
            
            return Response.Builder.representation("foo", "text/plain").build();
        }
    }
    
    public void testIfUnmodifiedSinceBeforeLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(412, r.getStatus());
    }    

    public void testIfUnmodifiedSinceAfterLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }    

    public void testIfModifiedSinceBeforeLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }    

    public void testIfModifiedSinceAfterLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(304, r.getStatus());
    }    

    public void testIfUnmodifiedSinceBeforeLastModified_IfModifiedSinceBeforeLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT");
        headers.putSingle("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(412, r.getStatus());
    }    

    public void testIfUnmodifiedSinceBeforeLastModified_IfModifiedSinceAfterLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT");
        headers.putSingle("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(412, r.getStatus());
    }    
    
    public void testIfUnmodifiedSinceAfterLastModified_IfModifiedSinceAfterLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT");
        headers.putSingle("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(304, r.getStatus());
    }    

    public void testIfUnmodifiedSinceAfterLastModified_IfModifiedSinceBeforeLastModified() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT");
        headers.putSingle("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT");
        HttpResponseContext r = callNoStatusCheck(LastModifiedResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }
        
    
    @UriTemplate("/")
    public static class EtagResource {
        @HttpContext PreconditionEvaluator evaluator;

        @HttpMethod("GET")
        public Response doGet() {
            Response r = evaluator.evaluate(new EntityTag("1"));
            if (r != null)
                return r;
            
            return Response.Builder.representation("foo", "text/plain").build();
        }
    }

    public void testIfMatchWithMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "\"1\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }
    
    public void testIfMatchWithoutMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "\"2\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(412, r.getStatus());
    }
    
    public void testIfMatchWildCard() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "*");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }
    
    public void testIfNonMatchWithMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-None-Match", "\"1\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(304, r.getStatus());
        assertEquals(new EntityTag("1"), r.getHttpHeaders().getFirst("ETag"));
    }
    
    public void testIfNonMatchWithoutMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-None-Match", "\"2\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }
    
    public void testIfNonMatchWildCard() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-None-Match", "*");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(304, r.getStatus());
        assertEquals(new EntityTag("1"), r.getHttpHeaders().getFirst("ETag"));
    }
    
    
    public void testIfMatchWithMatchingETag_IfNonMatchWithMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "\"1\"");
        headers.putSingle("If-None-Match", "\"1\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(304, r.getStatus());
    }
    
    public void testIfMatchWithMatchingETag_IfNonMatchWithoutMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "\"1\"");
        headers.putSingle("If-None-Match", "\"2\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(200, r.getStatus());
    }
    
    public void testIfMatchWithoutMatchingETag_IfNonMatchWithMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "\"2\"");
        headers.putSingle("If-None-Match", "\"1\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(412, r.getStatus());
    }
    
    public void testIfMatchWithoutMatchingETag_IfNonMatchWithoutMatchingETag() {
        MultivaluedMap<String, String> headers = new RequestHttpHeadersImpl();
        headers.putSingle("If-Match", "\"2\"");
        headers.putSingle("If-None-Match", "\"2\"");
        HttpResponseContext r = callNoStatusCheck(EtagResource.class, "GET", "/", headers, "");
        assertEquals(412, r.getStatus());
    }
}
