/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.server.impl.model;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.core.header.AcceptableLanguageTag;
import com.sun.jersey.core.header.AcceptableMediaType;
import com.sun.jersey.core.header.AcceptableToken;
import com.sun.jersey.core.header.LanguageTag;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

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
        return (contentTypeString != null) ? MediaType.valueOf(contentTypeString) : null;
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
            return MediaType.valueOf(contentType.toString());
    }

    /**
     * Get the content language as a Locale instance.
     *
     * @param request The HTTP request.
     * @return the content lanuage as a locale instance.
     */
    public static Locale getContentLanguageAsLocale(HttpRequestContext request) {
        return HttpHelper.getLanguageTagAsLocale(request.getRequestHeaders().
                getFirst(HttpHeaders.CONTENT_LANGUAGE));
    }


    public static Locale getLanguageTagAsLocale(String language) {
        if (language == null)
            return null;

        try {
            return new LanguageTag(language).getAsLocale();
        } catch (java.text.ParseException e) {
            throw clientError("Bad Content-Language field: " + language, e);
        }
    }

    /**
     * Get the list of Media type from the "Accept" of an HTTP request.
     * <p>
     * @param request The HTTP request.
     * @return The list of MediaType. This list
     *         is ordered with the highest quality acceptable Media type occuring first
     *         (see {@link MediaTypes#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     *         If no "Accept" is present then a list with a single item of the Media
     *         type "*\\/*" is returned.
     */
    public static List<AcceptableMediaType> getAccept(HttpRequestContext request) {
        final String accept = request.getHeaderValue("Accept");
        if (accept == null || accept.length() == 0) {
            return MediaTypes.GENERAL_ACCEPT_MEDIA_TYPE_LIST;
        }
        try {
            return HttpHeaderReader.readAcceptMediaType(accept);
        } catch (java.text.ParseException e) {
            throw clientError(ImplMessages.BAD_ACCEPT_FIELD(accept), e);
        }
    }
    
    public static List<AcceptableMediaType> getAccept(HttpRequestContext request,
            List<QualitySourceMediaType> priorityMediaTypes) {
        final String accept = request.getHeaderValue("Accept");
        if (accept == null || accept.length() == 0) {
            return MediaTypes.GENERAL_ACCEPT_MEDIA_TYPE_LIST;
        }
        try {
            return HttpHeaderReader.readAcceptMediaType(accept, priorityMediaTypes);
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
            return HttpHeaderReader.readAcceptLanguage(acceptLanguage);
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
            return HttpHeaderReader.readAcceptToken(acceptCharset);
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
            return HttpHeaderReader.readAcceptToken(acceptEncoding);
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
     *        (see {@link MediaTypes#ACCEPT_MEDIA_TYPE_COMPARATOR}).
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