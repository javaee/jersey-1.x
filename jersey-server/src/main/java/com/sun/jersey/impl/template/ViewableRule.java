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

package com.sun.jersey.impl.template;

import com.sun.jersey.spi.template.TemplateProcessor;
import com.sun.jersey.spi.template.TemplateContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * A viewable rule that defers the request to a template. If a template
 * is not available then the rule is not accepted.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ViewableRule implements UriRule {
    
    @Context TemplateContext tc;
    
    public ViewableRule() {
    }
    
    public final boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        final HttpRequestContext request = context.getRequest();
        // Only accept GET requests
        if (!request.getMethod().equals("GET"))
            return false;
        
        // Obtain the template path
        final String templatePath = (path.length() > 0) ? 
            context.getMatchResult().group(1) :
            "";
        
        final String absoluteTemplatePath = getAbsolutePath(resource.getClass(), 
                templatePath); 
        
        for (TemplateProcessor t : tc.getTemplateProcessors()) {
            String resolvedPath = t.resolve(absoluteTemplatePath);
            if (resolvedPath != null) {
                 final HttpResponseContext response = context.getResponse();
               
                response.setResponse(
                    Response.ok(new ResolvedViewable(t, resolvedPath, resource)).
                    build()
                    );
                return true;
            }
        }
        return false;
    }
    
    private String getAbsolutePath(Class<?> resourceClass, String path) {
        if (path == null || path.length() == 0 || path.equals("/")) {
            path = "index";
        } else if (path.startsWith("/")) {
            return path;
        }

        return getAbsolutePath(resourceClass) + '/' + path;
    }

    private String getAbsolutePath(Class<?> resourceClass) {
        return "/" + resourceClass.getName().replace('.', '/').replace('$', '/');
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
                    UriBuilder.fromUri(context.getUriInfo().
                    getAbsolutePath()).path("/").build()
                ).build()
                );
        return true;
    }
}