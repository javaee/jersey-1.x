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

import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import com.sun.jersey.server.probes.UriRuleProbeProvider;
import java.util.regex.MatchResult;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Adapter to an existing rule that accepts the rule using the last capturing 
 * group value as the URI path.
 * <p>
 * This rule assumes that the pattern used to match this rule has certain
 * contraints. If a pattern contains one or more capturing groups then the
 * last capturing group MUST occur at the end of the pattern and MUST be
 * '(/.*)?' or '(/)?'.
 * <p>
 * If the source from which the pattern was derived ends in a '/'
 * and the matched path does not end in a '/' then a temporary redirect 
 * response is returned with a path that is the matched path appened with '/'.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class RightHandPathRule implements UriRule {
    private final boolean redirect;
    
    private final boolean patternEndsInSlash;

    private final UriRule rule;
    
    /**
     * @param redirect if true return a temporary redirect response if the
     *        path does not end in '/' and the pattern ends in '/'.
     * @param patternEndsInSlash true if the pattern used to match with rule
     *        end in a '/', otherwise false.
     * @param rule the URI rule that is adapted.
     */
    public RightHandPathRule(boolean redirect, boolean patternEndsInSlash, UriRule rule) {
        assert rule != null;
        
        this.redirect = redirect;
        this.patternEndsInSlash = patternEndsInSlash;
        this.rule = rule;
    }
    
    public final boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        UriRuleProbeProvider.ruleAccept(RightHandPathRule.class.getSimpleName(), path,
                resource);
        
        String rhpath = getRightHandPath(context.getMatchResult());

        if (rhpath.length() == 0) {
            // Redirect to path ending with a '/' if pattern
            // ends in '/' and redirect is true
            if (patternEndsInSlash && redirect) {
                if (context.isTracingEnabled()) {
                    context.trace(
                            String.format("accept right hand path redirect: \"%s\" to \"%s/\"",
                            path,
                            path));
                }
                return redirect(context);
            }
            
            context.pushRightHandPathLength(0);
        } else if (rhpath.length() == 1) {
            // Path is '/', no match if pattern does not end in a '/'
            // and redirect is true
            if (!patternEndsInSlash && redirect)
                return false;

            // Consume the '/'
            rhpath = "";
            context.pushRightHandPathLength(0);
        } else {
            if (patternEndsInSlash) {
                context.pushRightHandPathLength(rhpath.length() - 1);
            } else {
                context.pushRightHandPathLength(rhpath.length());
            }
        }

        if (context.isTracingEnabled()) {
            CharSequence lhpath = path.subSequence(0, path.length() - rhpath.length());
            context.trace(
                    String.format("accept right hand path %s: \"%s\" -> \"%s\" : \"%s\"",
                    context.getMatchResult(),
                    path,
                    lhpath,
                    rhpath));
        }

        // Accept using the right hand path
        return rule.accept(rhpath, resource, context);
    }
    
    /**
     * Get the right hand path from the match result. The right hand path is
     * the last group (if present).
     * 
     * @param mr the match result.
     * @return the right hand path, or the empty string if there is no last
     *         group.
     */
    private String getRightHandPath(MatchResult mr) {        
        final String rhp = (mr.groupCount() > 0) ? mr.group(mr.groupCount()) : "";
        return (rhp != null) ? rhp : "";
    }

    /**
     * Redirect to a URI that ends in a slash.
     * 
     * TODO use the complete URI.
     */
    private boolean redirect(UriRuleContext context) {
        final HttpResponseContext response = context.getResponse();
        
        response.setResponse(
                Response.temporaryRedirect(
                    context.getUriInfo().getRequestUriBuilder().path("/").build()
                ).build()
            );
        return true;
    }
}