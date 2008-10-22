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
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.api.MultivaluedMapImpl;
import com.sun.jersey.impl.model.ResourceClass;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
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
        if (properties != null) {
            return properties;
        }

        return properties = new HashMap<String, Object>();
    }

    // UriMatchResultContext
    private MatchResult matchResult;

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }
    
    // UriRuleContext
    private final LinkedList<Object> resources = new LinkedList<Object>();
    private final LinkedList<MatchResult> matchResults = new LinkedList<MatchResult>();
    private final LinkedList<String> paths = new LinkedList<String>();
    private final LinkedList<UriTemplate> templates = new LinkedList<UriTemplate>();

    public Object getResource(Class resourceClass) {
        final ResourceClass rc = app.getResourceClass(resourceClass);
        return rc.provider.getInstance(app.getResourceComponentProvider(), this);
    }

    public UriRules<UriRule> getRules(Class resourceClass) {
        final ResourceClass rc = app.getResourceClass(resourceClass);
        return rc.getRules();
    }

    public void pushParameterValues(List<String> names) {
        if (encodedTemplateValues == null) {
            encodedTemplateValues = new MultivaluedMapImpl();
        }

        int i = 1;
        for (String name : names) {
            final String value = matchResult.group(i++);
            encodedTemplateValues.addFirst(name, value);

            if (decodedTemplateValues != null) {
                decodedTemplateValues.addFirst(
                        UriComponent.decode(name, UriComponent.Type.PATH_SEGMENT),
                        UriComponent.decode(value, UriComponent.Type.PATH));
            }
        }

        matchResults.addFirst(matchResult);
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
            if (decodedTemplateValues != null) {
                return decodedTemplateValues;
            }

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
    
    public List<MatchResult> getMatchedResults() {
        return matchResults;
    }

    public List<UriTemplate> getMatchedTemplates() {
        return templates;
    }

    public List<PathSegment> getPathSegments(String name) {
        return getPathSegments(name, true);
    }

    public List<PathSegment> getPathSegments(String name, boolean decode) {
        int[] bounds = getPathParameterBounds(name);
        if (bounds != null) {
            String path = matchResults.getLast().group();
            // Work out how many path segments are up to the start
            // and end position of the matching path parameter value
            // This assumes that the path always starts with a '/'
            int segmentsStart = 0;
            for (int x = 0; x < bounds[0]; x++) {
                if (path.charAt(x) == '/') {
                    segmentsStart++;
                }
            }           
            int segmentsEnd = segmentsStart;
            for (int x = bounds[0]; x < bounds[1]; x++) {
                if (path.charAt(x) == '/') {
                    segmentsEnd++;
                }
            }

            return getPathSegments(decode).subList(segmentsStart - 1, segmentsEnd);
        } else
            return Collections.emptyList();
    }

    private int[] getPathParameterBounds(String name) {
        Iterator<UriTemplate> iTemplate = templates.iterator();
        Iterator<MatchResult> iMatchResult = matchResults.iterator();
        while (iTemplate.hasNext()) {
            MatchResult mr = iMatchResult.next();
            // Find the index of path parameter
            int pIndex = getLastPathParameterIndex(name, iTemplate.next());
            if (pIndex != -1) {
                int pathLength = mr.group().length();
                int segmentIndex = mr.end(pIndex + 1);
                int groupLength = segmentIndex - mr.start(pIndex + 1);

                // Find the absolute position of the end of the
                // capturing group in the request path
                while (iMatchResult.hasNext()) {
                    mr = iMatchResult.next();
                    segmentIndex += mr.group().length() - pathLength;
                    pathLength = mr.group().length();
                }
                int[] bounds = {segmentIndex - groupLength, segmentIndex};
                return bounds;
            }
        }
        return null;
    }

    private int getLastPathParameterIndex(String name, UriTemplate t) {
        int i = 0;
        int pIndex = -1;
        for (String parameterName : t.getTemplateVariables()) {
            if (parameterName.equals(name)) {
                pIndex = i;
            }
            i++;
        }
        return pIndex;
    }
}