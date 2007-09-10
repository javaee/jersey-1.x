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

package com.sun.ws.rest.impl.model;

import com.sun.ws.rest.spi.dispatch.ResourceDispatchContext;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.spi.dispatch.ResourceDispatcher;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class BaseResourceClass implements ResourceDispatcher {
    
    protected final List<URITemplateDispatcher> dispatchers;
    
    protected BaseResourceClass() {
        this.dispatchers = new ArrayList<URITemplateDispatcher>();
    }
    
    // ResourceDispatcher 
    
    public boolean dispatch(ResourceDispatchContext context, Object node, String path) {
        for (final URITemplateDispatcher d : dispatchers) {
            if (context.matchLeftHandPath(d.getTemplate(), path)) {
                // Get the right hand side of the path
                path = context.getRightHandPath();
                if (path == null) {
                    // Redirect to path ending with a '/' if template
                    // ends in '/'
                    if (d.getTemplate().endsWithSlash())
                        return redirect(context);
                } else if (path.length() == 1) {
                    // No matchLeftHandPath if path ends in '/' but template does not
                    if (!d.getTemplate().endsWithSlash())
                        return false;
                    
                    // Consume the '/'
                    path = null;
                }
                
                return d.dispatch(context, node, path);
            }
        }
        
        return false;
    }
    
    private boolean redirect(ResourceDispatchContext context) {
        HttpRequestContext request = context.getHttpRequestContext();
        HttpResponseContext response = context.getHttpResponseContext();
        
        response.setResponse(
                ResponseBuilderImpl.temporaryRedirect(
                    UriBuilder.fromUri(request.getAbsolute()).path("/").build()
                ).build()
                );
        return true;
    }
}
