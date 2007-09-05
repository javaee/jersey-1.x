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

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.ContainerProvider;
import com.sun.ws.rest.spi.container.WebApplication;

/**
 * HTTP handler container provider.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpHandlerContainerProvider implements ContainerProvider<HttpHandler> {
    
    public HttpHandler createContainer(Class<HttpHandler> type, 
            ResourceConfig resourceConfig, WebApplication application) throws ContainerException {
        if (type != HttpHandler.class)
            return null;
        
        HttpHandlerContainer c = new HttpHandlerContainer(application);
        application.initiate(c, resourceConfig);
        return c;
    }
}
