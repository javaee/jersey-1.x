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

package com.sun.jersey.impl.application;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.uri.ExtendedUriInfo;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.impl.MultivaluedMapImpl;
import com.sun.jersey.impl.model.ResourceClass;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class WebApplicationContext implements UriRuleContext, ExtendedUriInfo {
    
    private final ContainerRequest request;
    
    private final ContainerResponse response;
    
    private final WebApplicationImpl app;

    private Map<String, Object> properties;
    
    public WebApplicationContext(WebApplicationImpl app, 
            ContainerRequest request, ContainerResponse response) {
        this.app = app;
        this.request = request;
        this.response = response;
    }

    
    // HttpContext
    
    public HttpRequestContext getRequest() {
        return request;
    }

    public HttpResponseContext getResponse() {
        return response;
    }
    
    public ExtendedUriInfo getUriInfo() {
        return this;
    }
    
    public Map<String, Object> getProperties() {
        if (properties != null)
            return properties;
        
        return properties = new HashMap<String, Object>();
    }
    
    // UriRuleContext

    private final LinkedList<Object> resources = new LinkedList<Object>();
    
    private final LinkedList<String> paths = new LinkedList<String>();
    
    private final LinkedList<UriTemplate> templates = new LinkedList<UriTemplate>();
    
    private final List<String> capturingGroupValues = new ArrayList<String>();
        
    public Object getResource(Class resourceClass) {
        final ResourceClass rc = app.getResourceClass(resourceClass);
        return rc.provider.getInstance(app.getResourceComponentProvider(), this);
    }

    public UriRules<UriRule> getRules(Class resourceClass) {
        final ResourceClass rc = app.getResourceClass(resourceClass);
        return rc.getRules();
    }

    public List<String> getGroupValues() {
        return capturingGroupValues;
    }
    
    public void setTemplateValues(List<String> names) {
        if (encodedTemplateValues == null)
            encodedTemplateValues = new MultivaluedMapImpl();
        
        int i = 0;
        for (String name : names) {
            final String value = capturingGroupValues.get(i++);
            encodedTemplateValues.addFirst(name, value);
            
            if (decodedTemplateValues != null) {
                decodedTemplateValues.addFirst(
                        UriComponent.decode(name, UriComponent.Type.PATH_SEGMENT),
                        UriComponent.decode(value, UriComponent.Type.PATH));                
            }
        }
    }
    
    public void pushResource(Object resource, UriTemplate template) {
         resources.addFirst(resource);
         templates.addFirst(template);
    }
    
    public void pushRightHandPathLength(int rhpathlen) {
        final String ep = request.getPath(false);
        paths.addFirst(ep.substring(0,
                ep.length() - rhpathlen));
    }
        
    // UriInfo, defer to HttpRequestContext
    
    private MultivaluedMapImpl encodedTemplateValues;
    
    private MultivaluedMapImpl decodedTemplateValues;
    
    public URI getBaseUri() {
        return request.getBaseUri();
    }
    
    public UriBuilder getBaseUriBuilder() {
        return request.getBaseUriBuilder();
    }
    
    public URI getAbsolutePath() {
        return request.getAbsolutePath();
    }
    
    public UriBuilder getAbsolutePathBuilder() {
        return request.getAbsolutePathBuilder();
    }
    
    public URI getRequestUri() {
        return request.getRequestUri();
    }
    
    public UriBuilder getRequestUriBuilder() {
        return request.getRequestUriBuilder();
    }
    
    public String getPath() {
        return request.getPath(true);
    }
    
    public String getPath(boolean decode) {
        return request.getPath(decode);
    }
    
    public List<PathSegment> getPathSegments() {
        return request.getPathSegments(true);
    }
    
    public List<PathSegment> getPathSegments(boolean decode) {
        return request.getPathSegments(decode);
    }
    
    public MultivaluedMap<String, String> getQueryParameters() {
        return request.getQueryParameters(true);
    }
    
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return request.getQueryParameters(decode);
    }

    // UriInfo, matching specific functionality
    
    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }
    
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        if (decode) {
            if (decodedTemplateValues != null)
                return decodedTemplateValues;
            
            decodedTemplateValues = new MultivaluedMapImpl();
            for (Map.Entry<String, List<String>> e : encodedTemplateValues.entrySet()) {
                List<String> l = new ArrayList<String>();
                for (String v : e.getValue()) {
                    l.add(UriComponent.decode(v, UriComponent.Type.PATH));
                }
                decodedTemplateValues.put(
                        UriComponent.decode(e.getKey(), UriComponent.Type.PATH_SEGMENT),
                        l);
            }
            
            return decodedTemplateValues;
        } else {
            return encodedTemplateValues;
        }
    }
    
    public List<String> getMatchedURIs() {
        return paths;
    }
    
    public List<String> getMatchedURIs(boolean decode) {
        throw new UnsupportedOperationException();
    }    
    
    public List<Object> getMatchedResources() {
        return resources;
    }    
    
    // ExtendedUriInfo
    
    public List<UriTemplate> getMatchedTemplates() {
        return templates;
    }    
    
    public PathSegment getPathSegment(String name) {
        return getPathSegment(name, true);
    }
    
    public PathSegment getPathSegment(String name, boolean decode) {
        int i = -1;
        for (UriTemplate t : templates) {
            if (i == -1)
                i = t.getPathSegmentIndex(name);
            else
                i += t.getNumberOfPathSegments();
        }
        
        return (i != -1) ? getPathSegments(decode).get(i) : null;
    }
}