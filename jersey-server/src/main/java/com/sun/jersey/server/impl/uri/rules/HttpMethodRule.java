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

package com.sun.jersey.server.impl.uri.rules;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.server.impl.model.method.ResourceMethod;
import com.sun.jersey.api.Responses;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.server.impl.template.ViewResourceMethod;
import com.sun.jersey.server.probes.UriRuleProbeProvider;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 * The rule for accepting an HTTP method.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpMethodRule implements UriRule {
    private final Map<String, ResourceMethodListPair> map;

    private final String allow;

    private final boolean isSubResource;

    public HttpMethodRule(
            Map<String, List<ResourceMethod>> methods) {
        this(methods, false);
    }

    private static class ResourceMethodListPair {
        final List<ResourceMethod> normal;

        final List<ResourceMethod> wildPriority;

        final List<QualitySourceMediaType> priorityMediaTypes;

        ResourceMethodListPair(List<ResourceMethod> normal) {
            this.normal = normal;
            if (correctOrder(normal)) {
                this.wildPriority = normal;
            } else {
                this.wildPriority = new ArrayList(normal.size());
                int i = 0;
                for (ResourceMethod method : normal) {
                    if (method.consumesWild()) {
                        wildPriority.add(i++, method);
                    } else {
                        wildPriority.add(method);
                    }
                }
            }

            List<QualitySourceMediaType> pmts = new LinkedList<QualitySourceMediaType>();
            for (ResourceMethod m : normal) {
                for (MediaType mt : m.getProduces()) {
                    QualitySourceMediaType qsmt = get(mt);
                    if (qsmt != null) {
                        if (qsmt.getQualitySource() > QualitySourceMediaType.DEFAULT_QUALITY_SOURCE_FACTOR) {
                            pmts.add(qsmt);
                        }
                    }
                }
            }

            Collections.sort(pmts, MediaTypes.QUALITY_SOURCE_MEDIA_TYPE_COMPARATOR);
            priorityMediaTypes = pmts.isEmpty() ? null : pmts;
        }

        QualitySourceMediaType get(MediaType mt) {
            if (mt instanceof QualitySourceMediaType) {
                return (QualitySourceMediaType)mt;
            } else if (mt.getParameters().containsKey("qs")) {
                return new QualitySourceMediaType(mt);
            } else {
                return null;
            }
        }

        boolean correctOrder(List<ResourceMethod> normal) {
            boolean comsumesNonWild = false;
            for (ResourceMethod method : normal) {
                if (method.consumesWild()) {
                    if (comsumesNonWild) return false;
                } else {
                    comsumesNonWild = true;
                }
            }

            return true;
        }
    }

    public HttpMethodRule(
            Map<String, List<ResourceMethod>> methods,
            boolean isSubResource) {
        this.map = new HashMap<String, ResourceMethodListPair>();
        for (Map.Entry<String, List<ResourceMethod>> e : methods.entrySet()) {
           this.map.put(e.getKey(), new ResourceMethodListPair(e.getValue()));
        }

        this.isSubResource = isSubResource;
        this.allow = getAllow(methods);
    }

    private String getAllow(Map<String, List<ResourceMethod>> methods) {
        StringBuilder s = new StringBuilder();
        for (String method : methods.keySet()) {
            if (s.length() > 0) s.append(",");

            s.append(method);
        }

        return s.toString();
    }

    public boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        UriRuleProbeProvider.ruleAccept(HttpMethodRule.class.getSimpleName(), path,
                resource);

        // If the path is not empty then do not accept
        if (path.length() > 0) return false;

        final HttpRequestContext request = context.getRequest();
        final HttpResponseContext response = context.getResponse();

        if (context.isTracingEnabled()) {
            final String currentPath = context.getUriInfo().getMatchedURIs().get(0);
            if (isSubResource) {
                final String prevPath = context.getUriInfo().getMatchedURIs().get(1);
                context.trace(String.format("accept sub-resource methods: \"%s\" : \"%s\", %s -> %s",
                        prevPath,
                        currentPath.substring(prevPath.length()),
                        context.getRequest().getMethod(),
                        ReflectionHelper.objectToString(resource)));
            } else {
                context.trace(String.format("accept resource methods: \"%s\", %s -> %s",
                        currentPath,
                        context.getRequest().getMethod(),
                        ReflectionHelper.objectToString(resource)));
            }
        }

        // Get the list of resource methods for the HTTP method
        ResourceMethodListPair methods = map.get(request.getMethod());
        if (methods == null) {
            // No resource methods are found
            response.setResponse(Responses.methodNotAllowed().
                    header("Allow", allow).build());
            // Allow any further matching rules to be processed
            return false;
        }

        // Get the list of matching methods
        List<MediaType> accept = getSpecificAcceptableMediaTypes(
                request.getAcceptableMediaTypes(),
                methods.priorityMediaTypes);

        final Matcher m = new Matcher();
        final MatchStatus s = m.match(methods, request.getMediaType(), accept);

        if (s == MatchStatus.MATCH) {
            // If there is a match choose the first method
            final ResourceMethod method = m.rmSelected;

            if (method instanceof ViewResourceMethod) {
                // Set the content type to the most acceptable
                if (!m.mSelected.isWildcardType() && !m.mSelected.isWildcardSubtype()) {
                    response.getHttpHeaders().putSingle("Content-Type", m.mSelected);
                }

                // Allow the view to be processed by the further matching view rule
                return false;

                // TODO what about resource specific request and response filters?
                // Should the viewable rule be responsible for those declared on
                // the class
            }

            // If a sub-resource method then need to push the resource
            // (again) as as to keep in sync with the ancestor URIs
            if (isSubResource) {
                context.pushResource(resource);
                // Set the template values
                context.pushMatch(method.getTemplate(), method.getTemplate().getTemplateVariables());
            }

            if (context.isTracingEnabled()) {
                if (isSubResource) {
                    context.trace(String.format("matched sub-resource method: @Path(\"%s\") %s",
                            method.getTemplate(),
                            method.getDispatcher()));
                } else {
                    context.trace(String.format("matched resource method: %s",
                            method.getDispatcher()));
                }
            }

            // Push the response filters
            context.pushContainerResponseFilters(method.getResponseFilters());

            // Process the request filter
            if (!method.getRequestFilters().isEmpty()) {
                ContainerRequest containerRequest = context.getContainerRequest();
                for (ContainerRequestFilter f : method.getRequestFilters()) {
                    containerRequest = f.filter(containerRequest);
                    context.setContainerRequest(containerRequest);
                }
            }

            // Dispatch to the resource method
            method.getDispatcher().dispatch(resource, context);

            // If the content type is not explicitly set then set it
            // to the selected media type, if a concrete media type
            // and @Produces is declared on the resource method or the resource
            // class
            Object contentType = response.getHttpHeaders().getFirst("Content-Type");
            if (contentType == null && method.isProducesDeclared()) {
                if (!m.mSelected.isWildcardType() && !m.mSelected.isWildcardSubtype()) {
                    response.getHttpHeaders().putSingle("Content-Type", m.mSelected);
                }
            }

            return true;
        } else if (s == MatchStatus.NO_MATCH_FOR_CONSUME) {
            response.setResponse(Responses.unsupportedMediaType().build());
            // Allow any further matching rules to be processed
            return false;
        } else if (s == MatchStatus.NO_MATCH_FOR_PRODUCE) {
            response.setResponse(Responses.notAcceptable().build());
            // Allow any further matching rules to be processed
            return false;
        }

        return true;
    }

    private enum MatchStatus {
        MATCH, NO_MATCH_FOR_CONSUME, NO_MATCH_FOR_PRODUCE
    }

    private static class Matcher extends LinkedList<ResourceMethod> {
        private MediaType mSelected = null;

        private ResourceMethod rmSelected = null;

        /**
         * Find the subset of methods that match the 'Content-Type' and 'Accept'.
         *
         * @param methods the list of resource methods
         * @param contentType the 'Content-Type'.
         * @param accept the 'Accept' as a list. This list
         *        MUST be ordered with the highest quality acceptable Media type
         *        occuring first (see
         *        {@link com.sun.ws.rest.impl.model.MimeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
         * @return the match status.
         */
        private MatchStatus match(
                ResourceMethodListPair methods,
                MediaType contentType,
                List<MediaType> acceptableMediaTypes) {

            List<ResourceMethod> selected = null;
            if (contentType != null) {
                // Find all methods that consume the MIME type of 'Content-Type'
                for (ResourceMethod method : methods.normal)
                    if (method.consumes(contentType))
                        add(method);

                if (isEmpty())
                    return MatchStatus.NO_MATCH_FOR_CONSUME;

                selected = this;
            } else {
                selected = methods.wildPriority;
            }


            for (MediaType amt : acceptableMediaTypes) {
                for (ResourceMethod rm : selected) {
                    for (MediaType p : rm.getProduces()) {
                        if (p.isCompatible(amt)) {
                            mSelected = MediaTypes.mostSpecific(p, amt);
                            rmSelected = rm;
                            return MatchStatus.MATCH;
                        }
                    }
                }
            }

            return MatchStatus.NO_MATCH_FOR_PRODUCE;
        }
    }

    /**
     * Get a list of media types that are acceptable for the response.
     *
     * @param acceptableMediaType the list of acceptable media types.
     * @param priorityMediaTypes the list of media types that take priority.
     * @return a singleton list containing the most specific media
     *         type for first media type in <code>priorityMediaTypes<code> that
     *         is compatible with an acceptable media type, otherwise the
     *         list of acceptable media type as returned by
     *         {@link #getAcceptableMediaTypes() }.
     *
     */
    public static List<MediaType> getSpecificAcceptableMediaTypes(
            final List<MediaType> acceptableMediaType,
            final List<? extends MediaType> priorityMediaTypes) {
        if (priorityMediaTypes != null) {
            for (MediaType pmt : priorityMediaTypes) {
                for (MediaType amt : acceptableMediaType) {
                    if (amt.isCompatible(pmt)) {
                        return Collections.singletonList(MediaTypes.mostSpecific(amt, pmt));
                    }
                }
            }
        }

        return acceptableMediaType;
    }
}