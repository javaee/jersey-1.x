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

package com.sun.ws.rest.impl.uri.rules;

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.model.HttpHelper;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethodList;
import com.sun.ws.rest.impl.model.method.ResourceMethodMap;
import com.sun.ws.rest.impl.response.Responses;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import com.sun.ws.rest.spi.uri.rules.UriRuleContext;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * The rule for accepting an HTTP method.
 * TODO 
 * Consider separate rules for HTTP method and sub-resource HTTP method
 * The former does not require redirection checking
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpMethodRule extends UriTemplateRule {
    private static final Logger LOGGER = 
            Logger.getLogger(HttpMethodRule.class.getName());

    private final ResourceMethodMap map;
    
    public HttpMethodRule(UriTemplateType template, ResourceMethodMap map) {
        super(template);
        this.map = map;
    }
          
    public boolean _accept(CharSequence path, Object resource, UriRuleContext context) {
        final HttpRequestContext request = context.getHttpContext().
                getHttpRequestContext();
        final HttpResponseContext response = context.getHttpContext().
                getHttpResponseContext();
        final String httpMethod = request.getHttpMethod();
        
        /* TODO
         * Replace with
         *   hasMessageBody(request.getHttpHeaders());
         * after unit tests have been fixed.
         */
        MediaType contentType = null;
        if (!httpMethod.equals("GET") && !httpMethod.equals("DELETE"))
            contentType = HttpHelper.getContentType(request);
        
        // Get the list of methods for the HTTP method
        ResourceMethodList methods = map.get(httpMethod);
        if (methods == null) {
            // Check for generic support of methods
            methods = map.get(null);
            if (methods == null) {
                // If no methods are found move on to next template
                Response r = new ResponseBuilderImpl(Responses.METHOD_NOT_ALLOWED).
                        header("Allow", map.getAllow()).build();
                response.setResponse(r);
                return true;
            }
        }

        // Get the list of matching methods
        List<MediaType> accept = request.getAcceptableMediaTypes();
        LinkedList<ResourceMethod> matches = 
            new LinkedList<ResourceMethod>();
        ResourceMethodList.MatchStatus s = methods.match(contentType, accept, matches);
        if (s == ResourceMethodList.MatchStatus.MATCH) {
            // If there is a match choose the first method
            ResourceMethod method = matches.get(0);

            // Set the template values
            setTemplateValues(context);
        
            method.getDispatcher().dispatch(resource, request, response);

            // Verify the response
            // TODO verification for HEAD
            if (!httpMethod.equals("HEAD"))
                verifyResponse(method, accept, response);                        
        } else if (s == ResourceMethodList.MatchStatus.NO_MATCH_FOR_CONSUME) {
            response.setResponse(Responses.UNSUPPORTED_MEDIA_TYPE);
        } else if (s == ResourceMethodList.MatchStatus.NO_MATCH_FOR_PRODUCE) {
            response.setResponse(Responses.NOT_ACCEPTABLE);
        }
        
        return true;
    }

    private boolean hasMessageBody(MultivaluedMap<String, String> headers) {
        return (headers.getFirst("Content-Length") != null || 
                headers.getFirst("Transfer-Encoding") != null);
    }
        
    private void verifyResponse(ResourceMethod method, 
            List<MediaType> accept,
            HttpResponseContext responseContext) {        
        Object entity = responseContext.getEntity();
        MediaType contentType = HttpHelper.getContentType(
                responseContext.getHttpHeaders().getFirst("Content-Type"));
        
        if (!responseContext.isCommitted() && contentType != null && entity == null) {
            String ct = contentType.toString();
            String error = "The \"Content-Type\" header is set to " + ct + ", but the response has no entity";
            LOGGER.severe(error);
            Response r = ResponseBuilderImpl.serverError().entity(error).type("text/plain").build();
            // TODO should this be ContainerException ???
            throw new WebApplicationException(r);            
        } else if (contentType != null && !method.produces(contentType)) {
            // Check if 'Content-Type' of the responseContext is a member of @ProduceMime
            // The resource is not honoring the @ProduceMime contract
            // The 'Content-Type' is not a member of @ProduceMime.
            // Check if the 'Content-Type' is acceptable
            if (!HttpHelper.produces(contentType, accept)) {
                String error = ImplMessages.RESOURCE_NOT_ACCEPTABLE(
                        method.getResourceClass(),
                        method.getMethod(),
                        contentType);
                LOGGER.severe(error);
                
                // The resource is returning a MIME type that is not acceptable
                // Return 500 Internal Server Error
                Response r = ResponseBuilderImpl.serverError().entity(error).type("text/plain").build();
                // TODO should this be ContainerException ???
                throw new WebApplicationException(r);
            } else {
                String error = ImplMessages.RESOURCE_MIMETYPE_NOT_IN_PRODUCE_MIME(
                        method.getResourceClass(),
                        method.getMethod(),
                        contentType,
                        method.getProduceMime());
                LOGGER.warning(error);
            }
        }
        
    }
}