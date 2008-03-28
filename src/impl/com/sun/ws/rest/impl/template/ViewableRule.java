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

package com.sun.ws.rest.impl.template;

import com.sun.ws.rest.spi.template.TemplateProcessor;
import com.sun.ws.rest.spi.template.TemplateContext;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRuleContext;
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
        if (!request.getHttpMethod().equals("GET"))
            return false;
        
        // Obtain the template path
        final String templatePath = (path.length() > 0) ? 
            context.getGroupValues().get(0) :
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
        final HttpRequestContext request = context.getRequest();
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