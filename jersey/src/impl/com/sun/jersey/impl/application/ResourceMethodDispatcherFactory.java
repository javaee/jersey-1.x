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

package com.sun.jersey.impl.application;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.impl.model.method.dispatch.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.service.ServiceFinder;
import com.sun.jersey.spi.template.TemplateProcessor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceMethodDispatcherFactory {
    private static final Logger LOGGER = Logger.getLogger(ResourceMethodDispatcherFactory.class.getName());
    
    private final Set<ResourceMethodDispatchProvider> dispatchers;
    
    public ResourceMethodDispatcherFactory(ComponentProviderCache componentProviderCache) {
        dispatchers = componentProviderCache.getProvidersAndServices(
                ResourceMethodDispatchProvider.class);
    }

    // TemplateContext
    
    public Set<ResourceMethodDispatchProvider> getDispatchers() {
        return dispatchers;
    }
    
    public RequestDispatcher getDispatcher(AbstractResourceMethod abstractResourceMethod) {
        for (ResourceMethodDispatchProvider rmdp : dispatchers) {
            try {
                RequestDispatcher d = rmdp.create(abstractResourceMethod);
                if (d != null)
                    return d;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                
                sw.write(ImplMessages.ERROR_PROCESSING_METHOD(
                        abstractResourceMethod.getMethod(), 
                        rmdp.getClass().getName()));
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                
                LOGGER.severe(sw.toString());
            }
        }
        
        return null;        
    }
}