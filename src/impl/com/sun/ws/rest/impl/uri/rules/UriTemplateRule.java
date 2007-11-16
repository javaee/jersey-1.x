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

package com.sun.ws.rest.impl.uri.rules;

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRuleContext;
import javax.ws.rs.core.UriBuilder;

/**
 * Abstract class for rules that are matched using a pattern generated from 
 * a URI template and a capturing group for the right hand path
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class UriTemplateRule implements UriRule {
    private final UriTemplateType template;
    
    public UriTemplateRule(UriTemplateType template) {
        this.template = template;
    }
    
    public UriTemplateType getTemplate() {
        return template;
    }
    
    public final boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        // Extract right hand path
        path = template.extractRightHandPath(context.capturingGroupValues());
        if (path.length() == 0) {
            // Redirect to path ending with a '/' if template
            // ends in '/'
            if (template.endsWithSlash())
                return redirect(context);
        } else if (path.length() == 1) {
            // No matchLeftHandPath if path ends in '/' but template does not
            if (!template.endsWithSlash())
                return false;

            // Consume the '/'
            path = "";
        }
        
        // Accept using the right hand path
        return _accept(path, resource, context);
    }
    
    private boolean redirect(UriRuleContext context) {
        final HttpRequestContext request = context.getHttpContext().
                getHttpRequestContext();
        final HttpResponseContext response = context.getHttpContext().
                getHttpResponseContext();
        
        response.setResponse(
                ResponseBuilderImpl.temporaryRedirect(
                    UriBuilder.fromUri(request.getAbsolute()).path("/").build()
                ).build()
                );
        return true;
    }
    
    /**
     * Accept the rule using the right hand path.
     *
     * @param path the the right hand URI path
     * @param resource the current resource instance
     * @param context the rule context
     * @return if true then the rule was accepted, if false then the rule was
     *         not accepted.
     */
    protected abstract boolean _accept(CharSequence path, Object resource, UriRuleContext context);
    
    protected final void setTemplateValues(UriRuleContext context) {
        context.commitTemplateValues(template.getTemplateVariables());
    }
}
