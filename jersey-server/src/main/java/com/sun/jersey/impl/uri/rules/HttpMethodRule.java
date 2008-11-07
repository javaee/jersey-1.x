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

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.api.MediaTypes;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.core.header.AcceptableMediaType;
import com.sun.jersey.impl.model.method.ResourceMethod;
import com.sun.jersey.api.Responses;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import java.util.Iterator;
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
    private final Map<String, List<ResourceMethod>> map;
    
    private final String allow;
            
    private final boolean isSubResource;
    
    public HttpMethodRule(Map<String, List<ResourceMethod>> methods) {
        this(methods, false);
    }
          
    public HttpMethodRule(Map<String, List<ResourceMethod>> methods, boolean isSubResource) {
        this.map = methods;
        this.isSubResource = isSubResource;
        this.allow = getAllow(methods);
    }
    
    private String getAllow(Map<String, List<ResourceMethod>> methods) {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (String method : methods.keySet()) {
            if (!first) s.append(",");
            first = false;
            
            s.append(method);
        }
        
        return s.toString();
    }

    public boolean accept(CharSequence path, Object resource, UriRuleContext context) {        
        // If the path is not empty then do not accept
        if (path.length() > 0) return false;
        
        final HttpRequestContext request = context.getRequest();
        final HttpResponseContext response = context.getResponse();
        
        // Get the list of resource methods for the HTTP method
        List<ResourceMethod> methods = map.get(request.getMethod());
        if (methods == null) {
            // No resource methods are found
            response.setResponse(Responses.methodNotAllowed().
                    header("Allow", allow).build());
            // Allow any further matching rules to be processed
            return false;
        }

        // Get the list of matching methods
        List<MediaType> accept = request.getAcceptableMediaTypes();

        final Matcher m = new Matcher();
        final MatchStatus s = m.match(methods, request.getMediaType(), accept);

        if (s == MatchStatus.MATCH) {
            // If there is a match choose the first method
            final ResourceMethod method = m.rmSelected;

            // If a sub-resource method then need to push the resource
            // (again) as as to keep in sync with the ancestor URIs
            if (isSubResource) {
                context.pushResource(resource, method.getTemplate());        
                // Set the template values
                context.pushParameterValues(method.getTemplate().getTemplateVariables());
            }
            
            method.getDispatcher().dispatch(resource, context);

            // If the content type is not explicitly set then set it
            // to the selected media type, if a concrete media type
            // and @Produces is declared on the resource method or the resource
            // class
            Object contentType = response.getHttpHeaders().getFirst("Content-Type");
            if (contentType == null && m.rmSelected.isProducesDeclared()) {
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
                List<ResourceMethod> methods,
                MediaType contentType,
                List<MediaType> acceptableMediaTypes) {

            List<ResourceMethod> selected = null;
            if (contentType != null) {
                // Find all methods that consume the MIME type of 'Content-Type'
                for (ResourceMethod method : methods)
                    if (method.consumes(contentType))
                        add(method);

                if (isEmpty())
                    return MatchStatus.NO_MATCH_FOR_CONSUME;

                selected = this;
            } else {
                selected = methods;
            }

            // Find all methods that produce the one or more Media types of 'Accept'
            int currentQuality = AcceptableMediaType.MINUMUM_QUALITY;
            for (ResourceMethod rm : selected) {
                int quality = -1;
                MediaType m = null;
                Iterator<MediaType> amtIterator = acceptableMediaTypes.iterator();
                while (quality < 0 && amtIterator.hasNext()) {
                    AcceptableMediaType amt = (AcceptableMediaType)amtIterator.next();
                    for (MediaType p : rm.getProduces()) {
                        if (p.isCompatible(amt)) {
                            quality = amt.getQuality();
                            m = MediaTypes.mostSpecific(p, amt);
                            break;
                        }
                    }
                }

                if (quality > currentQuality) {
                    // Match and of a higher quality than the pervious match
                    currentQuality = quality;
                    mSelected = m;
                    rmSelected = rm;
                }
            }

            if (mSelected == null)
                return MatchStatus.NO_MATCH_FOR_PRODUCE;

            return MatchStatus.MATCH;
        }
    }
}