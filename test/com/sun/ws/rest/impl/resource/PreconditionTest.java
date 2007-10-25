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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.util.GregorianCalendar;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.PreconditionEvaluator;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class PreconditionTest extends AbstractResourceTester {
    
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
        initiateWebApplication(LastModifiedResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ResponseInBound.class);
        assertEquals(412, response.getStatus());
    }    

    public void testIfUnmodifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        resourceProxy("/").
                request("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ResponseInBound.class);
    }    

    public void testIfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        resourceProxy("/").
                request("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ResponseInBound.class);
    }    

    public void testIfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ResponseInBound.class);
        assertEquals(304, response.getStatus());
    }    

    public void testIfUnmodifiedSinceBeforeLastModified_IfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ResponseInBound.class);
        assertEquals(412, response.getStatus());
    }    

    public void testIfUnmodifiedSinceBeforeLastModified_IfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Unmodified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ResponseInBound.class);
        assertEquals(412, response.getStatus());
    }    
    
    public void testIfUnmodifiedSinceAfterLastModified_IfModifiedSinceAfterLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                header("If-Modified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                get(ResponseInBound.class);
        assertEquals(304, response.getStatus());
    }    

    public void testIfUnmodifiedSinceAfterLastModified_IfModifiedSinceBeforeLastModified() {
        initiateWebApplication(LastModifiedResource.class);
        resourceProxy("/").
                request("If-Unmodified-Since", "Tue, 2 Jan 2007 00:00:00 GMT").
                header("If-Modified-Since", "Sat, 30 Dec 2006 00:00:00 GMT").
                get(ResponseInBound.class);
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
        initiateWebApplication(EtagResource.class);
        resourceProxy("/").
                request("If-Match", "\"1\"").
                get(ResponseInBound.class);
    }
    
    public void testIfMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Match", "\"2\"").
                get(ResponseInBound.class);
        assertEquals(412, response.getStatus());        
    }
    
    public void testIfMatchWildCard() {
        initiateWebApplication(EtagResource.class);
        resourceProxy("/").
                request("If-Match", "*").
                get(ResponseInBound.class);
    }
    
    public void testIfNonMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-None-Match", "\"1\"").
                get(ResponseInBound.class);
        assertEquals(304, response.getStatus());
        assertEquals(new EntityTag("1"), response.getEntityTag());
    }
    
    public void testIfNonMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        resourceProxy("/").
                request("If-None-Match", "\"2\"").
                get(ResponseInBound.class);
    }
    
    public void testIfNonMatchWildCard() {
        initiateWebApplication(EtagResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-None-Match", "*").
                get(ResponseInBound.class);
        assertEquals(304, response.getStatus());
        assertEquals(new EntityTag("1"), response.getEntityTag());
    }
    
    
    public void testIfMatchWithMatchingETag_IfNonMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Match", "\"1\"").
                header("If-None-Match", "\"1\"").
                get(ResponseInBound.class);
        assertEquals(304, response.getStatus());
        assertEquals(new EntityTag("1"), response.getEntityTag());
    }
    
    public void testIfMatchWithMatchingETag_IfNonMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        resourceProxy("/").
                request("If-Match", "\"1\"").
                header("If-None-Match", "\"2\"").
                get(ResponseInBound.class);
    }
    
    public void testIfMatchWithoutMatchingETag_IfNonMatchWithMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Match", "\"2\"").
                header("If-None-Match", "\"1\"").
                get(ResponseInBound.class);
        assertEquals(412, response.getStatus());
    }
    
    public void testIfMatchWithoutMatchingETag_IfNonMatchWithoutMatchingETag() {
        initiateWebApplication(EtagResource.class);
        ResponseInBound response = resourceProxy("/", false).
                request("If-Match", "\"2\"").
                header("If-None-Match", "\"2\"").
                get(ResponseInBound.class);
        assertEquals(412, response.getStatus());
    }
}
