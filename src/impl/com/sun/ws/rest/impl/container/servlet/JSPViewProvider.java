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
import com.sun.ws.rest.spi.view.ViewProvider;
import com.sun.ws.rest.spi.view.View;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JSPViewProvider implements ViewProvider {
    
    static final class JSPView implements View {
        private final String path;
        private final MediaType mediaType;
        
        JSPView(String path) {
            this.path = path;
            this.mediaType = new MediaType("text/html");
        }
                
        public void dispatch(Object it, 
                HttpRequestContext request, HttpResponseContext response) {
            ((HttpResponseAdaptor)response).forwardTo(path, it);                
        }

        public MediaType getProduceMime() {
            return mediaType;
        }
    }
    
    public View createView(Object containerMemento, String absolutePath) throws ContainerException {
        if (!absolutePath.endsWith(".jsp"))
            return null;
        
        if (!(containerMemento instanceof ServletAdaptor)) {
            throw new ContainerException("Java Server Pages not supported by the container");
        }
        
        ServletAdaptor a = (ServletAdaptor)containerMemento;
        try {
            if (a.getServletContext().getResource(absolutePath) == null) {
                // TODO log
                return null;
            }
        } catch (MalformedURLException ex) {
            // TODO log
            return null;
        }
        
        return new JSPView(absolutePath);
    }
}