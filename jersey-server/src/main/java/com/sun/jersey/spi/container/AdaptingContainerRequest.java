/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.spi.container;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;

/**
 * An adapting in-bound HTTP request that may override the behaviour of
 * {@link ContainerRequest}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class AdaptingContainerRequest extends ContainerRequest {
   
    /**
     * The adapted container request.
     */
    protected final ContainerRequest acr;

    /**
     * Create the adapting container request.
     *
     * @param acr the container request to adapt.
     */
    protected AdaptingContainerRequest(ContainerRequest acr) {
        super(acr);
        this.acr = acr;
    }

    @Override
    public Map<String, Object> getProperties() {
        return acr.getProperties();
    }

    @Override
    public void setMethod(String method) {
        acr.setMethod(method);
    }

    @Override
    public void setUris(URI baseUri, URI requestUri) {
        acr.setUris(baseUri, requestUri);
    }

    @Override
    public InputStream getEntityInputStream() {
        return acr.getEntityInputStream();
    }

    @Override
    public void setEntityInputStream(InputStream entity) {
        acr.setEntityInputStream(entity);
    }

    @Override
    public void setHeaders(InBoundHeaders headers) {
        acr.setHeaders(headers);
    }

    @Override
    public void setSecurityContext(SecurityContext securityContext) {
        acr.setSecurityContext(securityContext);
    }

    @Override
    public MessageBodyWorkers getMessageBodyWorkers() {
        return acr.getMessageBodyWorkers();
    }

    @Override
    public URI getBaseUri() {
        return acr.getBaseUri();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return acr.getBaseUriBuilder();
    }

    @Override
    public URI getRequestUri() {
        return acr.getRequestUri();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return acr.getRequestUriBuilder();
    }

    @Override
    public URI getAbsolutePath() {
        return acr.getAbsolutePath();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return acr.getAbsolutePathBuilder();
    }

    @Override
    public String getPath() {
        return acr.getPath();
    }

    @Override
    public String getPath(boolean decode) {
        return acr.getPath(decode);
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return acr.getPathSegments();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        return acr.getPathSegments(decode);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return acr.getQueryParameters();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return acr.getQueryParameters(decode);
    }

    @Override
    public String getHeaderValue(String name) {
        return acr.getHeaderValue(name);
    }

    @Override
    public MediaType getAcceptableMediaType(List<MediaType> mediaTypes) {
        return acr.getAcceptableMediaType(mediaTypes);
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes(List<QualitySourceMediaType> priorityMediaTypes) {
        return acr.getAcceptableMediaTypes(priorityMediaTypes);
    }

    @Override
    public MultivaluedMap<String, String> getCookieNameValueMap() {
        return acr.getCookieNameValueMap();
    }

    @Override
    public <T> T getEntity(Class<T> type) throws WebApplicationException {
        return acr.getEntity(type);
    }

    @Override
    public <T> T getEntity(Class<T> type, Type genericType, Annotation[] as) throws WebApplicationException {
        return acr.getEntity(type, genericType, as);
    }

    @Override
    public Form getFormParameters() {
        return acr.getFormParameters();
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return acr.getRequestHeader(name);
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return acr.getRequestHeaders();
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return acr.getAcceptableMediaTypes();
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return acr.getAcceptableLanguages();
    }

    @Override
    public MediaType getMediaType() {
        return acr.getMediaType();
    }

    @Override
    public Locale getLanguage() {
        return acr.getLanguage();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return acr.getCookies();
    }

    @Override
    public String getMethod() {
        return acr.getMethod();
    }

    @Override
    public Variant selectVariant(List<Variant> variants) throws IllegalArgumentException {
        return acr.selectVariant(variants);
    }

    @Override
    public ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        return acr.evaluatePreconditions(eTag);
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        return acr.evaluatePreconditions(lastModified);
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        return acr.evaluatePreconditions(lastModified, eTag);
    }

    @Override
    public ResponseBuilder evaluatePreconditions() {
        return acr.evaluatePreconditions();
    }

    @Override
    public Principal getUserPrincipal() {
        return acr.getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role) {
        return acr.isUserInRole(role);
    }

    @Override
    public boolean isSecure() {
        return acr.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return acr.getAuthenticationScheme();
    }
}