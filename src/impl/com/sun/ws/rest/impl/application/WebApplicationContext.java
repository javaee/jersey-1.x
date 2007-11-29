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
import com.sun.ws.rest.api.model.AbstractResourceConstructor;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractorFactory;
import com.sun.ws.rest.impl.view.ViewFactory;
import com.sun.ws.rest.impl.view.ViewType;
import com.sun.ws.rest.spi.container.ContainerRequest;
import com.sun.ws.rest.spi.container.ContainerResponse;
import com.sun.ws.rest.spi.resource.ResourceProviderContext;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRuleContext;
import com.sun.ws.rest.spi.uri.rules.UriRules;
import com.sun.ws.rest.spi.view.View;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class WebApplicationContext implements 
        HttpContextAccess, ResourceProviderContext, 
        UriRuleContext {
    private final ContainerRequest request;
    
    private final ContainerResponse response;
    
    private final WebApplicationImpl app;

    /* package */ WebApplicationContext(WebApplicationImpl app, 
            ContainerRequest request, ContainerResponse response) {
        this.app = app;
        this.request = request;
        this.response = response;
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
        
        return Response.ok(vt, v.getProduceMime()).build();
    }

    
    // ResourceProviderContext
            
    public void injectDependencies(Object resource) {
        app.injectResources(resource);
        
        // TODO defer to other injection providers
    }    

    public Object[] getParameterValues(AbstractResourceConstructor abstractResourceConstructor) {
        // TODO the extractors can be pre-calculated and associated with
        // the the resource class
        ParameterExtractor[] extractors = ParameterExtractorFactory.
                createExtractorsForConstructor(abstractResourceConstructor);
        Object[] values = new Object[extractors.length];
        for (int i = 0; i < extractors.length; i++) {
            if (extractors[i] == null)
                values[i] = null;
            else
                values[i] = extractors[i].extract(getHttpRequestContext());
        }
        return values;
    }

    // UriRuleContext

    private Object it;
    
    private final List<String> capturingGroupValues = new ArrayList<String>();
        
    public HttpContextAccess getHttpContext() {
        return this;
    }

    public Object getResource(Class resourceClass) {
        final ResourceClass rc = app.getResourceClass(resourceClass);
        return it = rc.resolver.getInstance(this);
    }

    public UriRules<UriRule> getRules(Class resourceClass) {
        final ResourceClass rc = app.getResourceClass(resourceClass);
        return rc.getRules();
    }

    public List<String> getGroupValues() {
        return capturingGroupValues;
    }
    
    public void setTemplateValues(List<String> names) {
        request.addTemplateValues(names, capturingGroupValues);
    }
}
