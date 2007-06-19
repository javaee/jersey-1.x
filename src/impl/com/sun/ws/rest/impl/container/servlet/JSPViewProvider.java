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

package com.sun.ws.rest.impl.container.servlet;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.spi.view.View;
import com.sun.ws.rest.spi.view.ViewProvider;
import java.io.IOException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JSPViewProvider implements ViewProvider {
    
    static final class JSPView implements View {
        final String path;
        
        JSPView(String path) {
            this.path = path;
        }
                
        public void process(Object it, 
                HttpRequestContext request, HttpResponseContext response) throws IOException, ContainerException {
            ((HttpResponseAdaptor)response).forwardTo(path, it);                
        }
    }
    
    public View createView(HttpRequestContext request, Object it, String resource) throws ContainerException {
        if (resource.endsWith(".jsp")) {
            if (!(request instanceof HttpRequestAdaptor)) {
                throw new ContainerException("Java Server Pages not supported by the container");
            }

            // TODO check for relative or absolute path
            // If relative then use 'it' location to produce
            // base path
            
            return new JSPView(resource);
        }
        
        return null;
    }
}