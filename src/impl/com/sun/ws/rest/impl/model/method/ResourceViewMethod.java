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

package com.sun.ws.rest.impl.model.method;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.model.MediaTypeList;
import com.sun.ws.rest.impl.model.MimeHelper;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.view.ViewType;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import com.sun.ws.rest.spi.view.View;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceViewMethod extends ResourceMethod {
    protected final View v;

    protected RequestDispatcher dispatcher;
    
    private class ViewMethodDispatcher implements RequestDispatcher {
        public void dispatch(final Object resource, 
                final HttpRequestContext requestContext, 
                final HttpResponseContext responseContext) {
            
            ViewType vt = new ViewType() {
                public void process() throws IOException {
                    v.dispatch(resource, requestContext, responseContext);
                }
            };
            responseContext.setResponse(ResponseBuilderImpl.representation(vt, v.getProduceMime()).build());
        }
    }
    
    public ResourceViewMethod(ResourceClass resourceClass, View v) throws ContainerException {
        super(resourceClass);
        this.consumeMime = MimeHelper.GENERAL_MEDIA_TYPE_LIST;
        this.produceMime = new MediaTypeList();
        this.produceMime.add(v.getProduceMime());
        this.httpMethod = "GET";
        
        this.v = v;
        this.dispatcher = new ViewMethodDispatcher();
    }
    
    public Method getMethod() {
        // TODO
        return null;
    }
    
    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }
    
}