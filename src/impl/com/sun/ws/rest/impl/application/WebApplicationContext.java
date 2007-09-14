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
import com.sun.ws.rest.impl.model.node.NodeDispatcherFactory;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.spi.dispatch.ResourceDispatchContext;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.view.ViewFactory;
import com.sun.ws.rest.impl.view.ViewType;
import com.sun.ws.rest.spi.container.ContainerRequest;
import com.sun.ws.rest.spi.container.ContainerResponse;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import com.sun.ws.rest.spi.resource.ResourceProviderContext;
import com.sun.ws.rest.spi.view.View;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class WebApplicationContext implements HttpContextAccess, ResourceDispatchContext, ResourceProviderContext {
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

    
    public Response createLocalForward(String path) throws ContainerException {
        final View v = ViewFactory.createView(app.containerMomento, path);
        if (v == null) {
            throw new ContainerException("No view for \"" + path + "\"");
        }

        ViewType vt = new ViewType() {
            public void process() throws IOException, ContainerException {
                v.dispatch(it, request, response);
            }
        };
        
        return ResponseBuilderImpl.representation(vt, v.getProduceMime()).build();
    }

    
    // Dispatching fields
    
    Object it;
    
    Map<String, String> templateValues = new HashMap<String, String>();
    
    // ResourceDispatchContext
    
    public HttpContextAccess getHttpContext() {
        return this;
    }
    
    public boolean dispatchTo(final Class nodeClass, final StringBuilder path) {
        final ResourceClass resourceClass = app.getResourceClass(nodeClass);
        final Object node = it = resourceClass.resolver.getInstance(this);
        return resourceClass.dispatch(this, node, path);
    }

    public boolean dispatchTo(final Object node, final StringBuilder path) {
        it = node;
        return app.getResourceClass(node.getClass()).dispatch(this, node, path);
    }
        
    public Map<String, String> getTemplateParameters() {
        return templateValues;
    }
    
    public void commitTemplateParameters(Map<String, String> templateParameters) {
        request.addTemplateValues(templateParameters);
    }
    
    
    // ResourceProviderContext
            
    public void injectDependencies(Object resource) {
        app.injectResources(resource);
        
        // TODO defer to other injection providers
    }    

    public Object[] getParameterValues(Constructor ctor) {
        ParameterExtractor[] extractors = NodeDispatcherFactory.processParameters(ctor);
        Object[] values = new Object[extractors.length];
        for (int i = 0; i < extractors.length; i++) {
            if (extractors[i] == null)
                values[i] = null;
            else
                values[i] = extractors[i].extract(getHttpRequestContext());
        }
        return values;
    }
    
    
}
