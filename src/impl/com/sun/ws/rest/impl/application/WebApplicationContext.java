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

package com.sun.ws.rest.impl.application;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.spi.dispatch.DispatchContext;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.view.ViewFactory;
import com.sun.ws.rest.impl.view.ViewType;
import com.sun.ws.rest.spi.container.ContainerRequest;
import com.sun.ws.rest.spi.container.ContainerResponse;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import com.sun.ws.rest.spi.resolver.WebResourceResolverListener;
import com.sun.ws.rest.spi.view.View;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class WebApplicationContext implements HttpContextAccess, ViewType, DispatchContext, WebResourceResolverListener {
    final ContainerRequest request;
    
    final ContainerResponse response;
    
    final WebApplicationImpl app;

    WebApplicationContext(WebApplicationImpl app, ContainerRequest request, ContainerResponse response) {
        this.request = request;
        this.response = response;
        this.app = app;
    }

    public HttpRequestContext getHttpRequestContext() {
        return request;
    }

    public HttpResponseContext getHttpResponseContext() {
        return response;
    }

    
    View v;

    public Response createLocalForward(String path) throws ContainerException {
        v = ViewFactory.createView(request, it, path);
        if (v == null) {
            throw new ContainerException("No view for \"" + path + "\"");
        }

        // Set the default view to "text/html"
        // TODO need to fix this, namely the View should specify the
        // static media type or set it when the view is processed
        return ResponseBuilderImpl.ok(this).type("text/html").build();
    }

    
    // View type
    
    public void process() throws IOException, ContainerException {
        v.process(it, request, response);
    }    

    
    // Dispatching logic 
    
    Object it;
    
    Map<String, String> templateValues = new HashMap<String, String>();
    
    // DispatchContext
    
    public HttpContextAccess getHttpContext() {
        return this;
    }
    
    public boolean dispatchTo(final Class nodeClass, final String path) {
        final ResourceClass resourceClass = app.getResourceClass(nodeClass);
        final Object node = it = resourceClass.resolver.resolve(request, this);
        return resourceClass.dispatch(this, node, path);
    }

    public boolean dispatchTo(final Object node, final String path) {
        it = node;
        return app.getResourceClass(node.getClass()).dispatch(this, node, path);
    }
    
    public boolean matchLeftHandPath(URITemplateType t, String path) {
        if (t.match(path, templateValues)) {
            setTemplateValues();
            return true;
        } else {
            return false;
        }
    }

    public String getRightHandPath() {
        return templateValues.get(null);
    }
    
    private void setTemplateValues() {
        final MultivaluedMap<String, String> m = request.getURIParameters();
        for (Map.Entry<String, String> e : templateValues.entrySet()) {
            m.putSingle(e.getKey(), e.getValue());
        }
    }
    
    // WebResourceResolverListener
            
    public void onInstantiation(Object resource) {
        app.injectResources(resource);
        
        // TODO defer to other injection providers
    }    
    
    
}
