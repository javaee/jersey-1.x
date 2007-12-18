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
import com.sun.ws.rest.impl.http.header.AcceptableLanguageTag;
import com.sun.ws.rest.impl.http.header.AcceptableMediaType;
import com.sun.ws.rest.impl.http.header.AcceptableToken;
import com.sun.ws.rest.impl.http.header.HttpHeaderFactory;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * Helper classes for HTTP.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpHelper {

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
        return (contentTypeString != null) ? MediaType.parse(contentTypeString) : null;
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
            return MediaType.parse(contentType.toString());
    }
    
    /**
     * Get the list of Media type from the "Accept" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The list of MediaType. This list
     *         is ordered with the highest quality acceptable Media type occuring first
     *         (see {@link MediaTypeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     *         If no "Accept" is present then a list with a single item of the Media
     *         type "*\\/*" is returned.
     */
    public static List<AcceptableMediaType> getAccept(HttpRequestContext request) {
        final String accept = request.getHeaderValue("Accept");
        if (accept == null || accept.length() == 0) {
            return MediaTypeHelper.GENERAL_ACCEPT_MEDIA_TYPE_LIST;
        }
        try {
            return HttpHeaderFactory.createAcceptMediaType(accept);
        } catch (java.text.ParseException e) {
            throw clientError(ImplMessages.BAD_ACCEPT_FIELD(accept), e);
        }
    }
    
    /**
     * Get the list of language tag from the "Accept-Language" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The list of LanguageTag. This list
     *         is ordered with the highest quality acceptable language tag occuring first.
     */
    public static List<AcceptableLanguageTag> getAcceptLangauge(HttpRequestContext request) {
        final String acceptLanguage = request.getHeaderValue("Accept-Language");
        if (acceptLanguage == null || acceptLanguage.length() == 0) {
            return Collections.singletonList(new AcceptableLanguageTag("*", null));
        }
        try {
            return HttpHeaderFactory.createAcceptLanguage(acceptLanguage);
        } catch (java.text.ParseException e) {
            throw clientError("Bad Accept-Language field: " + acceptLanguage, e);
        }
    }
    
    /**
     * Get the list of language tag from the "Accept-Charset" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The list of AcceptableToken. This list
     *         is ordered with the highest quality acceptable charset occuring first.
     */
    public static List<AcceptableToken> getAcceptCharset(HttpRequestContext request) {
        final String acceptCharset = request.getHeaderValue("Accept-Charset");
        try {
            if (acceptCharset == null || acceptCharset.length() == 0) {
                return Collections.singletonList(new AcceptableToken("*"));
            }
            return HttpHeaderFactory.createAcceptCharset(acceptCharset);
        } catch (java.text.ParseException e) {
            throw clientError("Bad Accept-Charset field: " + acceptCharset, e);
        }
    }
    
    /**
     * Get the list of language tag from the "Accept-Charset" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The list of AcceptableToken. This list
     *         is ordered with the highest quality acceptable charset occuring first.
     */
    public static List<AcceptableToken> getAcceptEncoding(HttpRequestContext request) {
        final String acceptEncoding = request.getHeaderValue("Accept-Encoding");
        try {
            if (acceptEncoding == null || acceptEncoding.length() == 0) {
                return Collections.singletonList(new AcceptableToken("*"));
            }
            return HttpHeaderFactory.createAcceptEncoding(acceptEncoding);
        } catch (java.text.ParseException e) {
            throw clientError("Bad Accept-Encoding field: " + acceptEncoding, e);
        }
    }
    
    private static WebApplicationException clientError(String message, Exception e) {        
        return new WebApplicationException(e, Response.status(400).
                entity(message).type("text/plain").build());
    }
    
    /**
     * Ascertain if an entity of a specific Media type is capable of being
     * produced from a list of Media type.
     *
     * @param contentType The Media type.
     * @param accept The list of Media types of entities that may be produced. This list
     *        MUST be ordered with the highest quality acceptable Media type occuring first
     *        (see {@link MediaTypeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
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