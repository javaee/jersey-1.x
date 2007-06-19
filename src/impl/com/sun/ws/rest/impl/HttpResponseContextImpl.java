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

package com.sun.ws.rest.impl;

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.spi.container.ContainerResponse;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class HttpResponseContextImpl implements ContainerResponse {
    private static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application/octet-stream");
        
    public static final Response EMPTY_RESPONSE = Response.Builder.noContent().build();
            
    private final HttpRequestContext requestContext;
    
    private Response response;
    
    private MultivaluedMap<String, Object> headers;
    
    public HttpResponseContextImpl(HttpRequestContext requestContext) {
        this.requestContext = requestContext;
        this.response = EMPTY_RESPONSE;
    }

    // HttpResponseContext
    
    public void setResponse(Response response) {
        setResponse(response, APPLICATION_OCTET_STREAM);
    }
    
    public void setResponse(Response response, MediaType contentType) {
        if (contentType == null)
            contentType = APPLICATION_OCTET_STREAM;
        
        this.response = (response != null) ? response : EMPTY_RESPONSE;
        this.headers = new ResponseHttpHeadersImpl();
        
        
        if (response instanceof ResponseImpl) {
            setResponseOptimal((ResponseImpl)response, contentType);
        } else {
            this.response.addMetadata(headers);
            setResponseNonOptimal(response, contentType);
        }        
    }
    
    public Response getResponse() {
        return response;
    }
            
    public MultivaluedMap<String, Object> getHttpHeaders() {
        if (headers == null)
            headers = new ResponseHttpHeadersImpl();
        return headers;
    }    

    //
    
    private void setResponseOptimal(ResponseImpl r, MediaType contentType) {
        r.addMetadataOptimal(headers, requestContext, contentType);
    }
    
    private void setResponseNonOptimal(Response r, MediaType contentType) {
        if (response.getEntity() != null && headers.getFirst("Content-Type") == null) {
            headers.putSingle("Content-Type", contentType);
        }
        
        Object location = headers.getFirst("Location");
        if (location != null) {
            if (location instanceof URI) {
                URI absoluteLocation = requestContext.getBaseURI().resolve((URI)location);
                headers.putSingle("Location", absoluteLocation);
            }
        }
    }
        
    @SuppressWarnings("unchecked")
    public String getHeaderValue(Object headerValue) {
        // TODO: performance, this is very slow
        HeaderProvider hp = ProviderFactory.newInstance().createHeaderProvider(headerValue.getClass());
        return hp.toString(headerValue);
    }
}