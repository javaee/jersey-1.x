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

package com.sun.ws.rest.impl.model;

import com.sun.ws.rest.api.core.HttpRequestContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import com.sun.ws.rest.impl.ImplMessages;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

/**
 * Helper classes for HTTP.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpHelper {
    /**
     * Set of characters for CTLs
     */
    private static final char[] CTLS = { 
         0,  1,  2, 3,  4,  5,  6,  7,  8,  9, 
        10, 11, 12, 13 ,14, 15, 16, 17, 18, 19,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
        31, 127,
    };
    
    /**
     * Set of characters for separator.
     */
    private static final char[] SEPARATORS = {
        '(',  ')', '<', '>', '@', ',', ';', 
        ':', '\\', '"', '/', '[', ']', '?', 
        '=', '{', '}', ' ', 9,        
    };
    
    /**
     * Set of invalid characters for token.
     */
    private static final Set<Character> invalidTokens = new HashSet<Character>();
    
    static {
        for (char c : CTLS)
            invalidTokens.add(c);
        for (char c : SEPARATORS)
            invalidTokens.add(c);
    }
    
    /**
     * Check if an HTTP method is valid according to token.
     *
     * @param httpMethod the http method.
     * @return true if the HTTP method is valid, otherwise false.
     */
    public static boolean isValidHttpMethod(String httpMethod) {
        for (int i = 0; i < httpMethod.length(); i++)
            if (invalidTokens.contains(httpMethod.charAt(i)))
                return false;
        
        return true;
    }
    
    /**
     * Get the content type from the "Content-Type" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The content type. If no "Content-Type is present then null is
     *         returned.
     */
    public static MediaType getContentType(HttpRequestContext request) {
        return getContentType(request.getRequestHeaders().getFirst("Content-Type"));
    }
    
    /**
     * Get the content type from a String.
     * <p>
     * @param contentTypeString the content type as a String.
     * @return The content type. If no "Content-Type is present then null is
     *         returned.
     */
    public static MediaType getContentType(String contentTypeString) {
        return (contentTypeString != null) ? new MediaType(contentTypeString) : null;
    }
    
    /**
     * Get the content type from an Object.
     * <p>
     * @param contentType the content type as an Object.
     * @return The content type. If no "Content-Type is present then null is
     *         returned.
     */
    public static MediaType getContentType(Object contentType) {
        if (contentType == null)
            return null;
        
        if (contentType instanceof MediaType)
            return (MediaType)contentType;
        else 
            return new MediaType(contentType.toString());
    }
    
    /**
     * Get the list of Media type from the "Accept" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The list of MediaType. This list
     *         is ordered with the highest quality acceptable Media type occuring first
     *         (see {@link MimeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     *         If no "Accept" is present then a list with a single item of the Media
     *         type "*\\/*" is returned.
     */
    public static List<MediaType> getAccept(HttpRequestContext request) {
        final List<String> accept = request.getRequestHeaders().get("Accept");
        if (accept == null || accept.isEmpty()) {   
            return MimeHelper.GENERAL_ACCEPT_MEDIA_TYPE_LIST;
        }
        
        String acceptString = accept.get(0);
        if (accept.size() > 1) {
            for (int i = 1; i < accept.size(); i++) {
                acceptString += "," + accept.get(i);
            }
        }
        
        try {
            return MimeHelper.createAcceptMediaTypes(acceptString);
        } catch (java.text.ParseException e) {
            throw clientError(ImplMessages.BAD_ACCEPT_FIELD(acceptString), e);
        }
    }
    
    private static WebApplicationException clientError(String message, Exception e) {        
        return new WebApplicationException(e, Response.serverError().
                status(400).entity(message).type("text/plain").build());
    }
    
    /**
     * Ascertain if an entity of a specific Media type is capable of being
     * produced from a list of Media type.
     *
     * @param contentType The Media type.
     * @param accept The list of Media types of entities that may be produced. This list
     *        MUST be ordered with the highest quality acceptable Media type occuring first
     *        (see {@link MimeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     * @return true if the Media type can be produced, otherwise false.
     */
    public static boolean produces(MediaType contentType, List<MediaType> accept) {
        for (MediaType a : accept) {
            if (a.getType().equals("*")) return true;
        
            if (contentType.isCompatible(a)) return true;
        }
        
        return false;
    }    
}