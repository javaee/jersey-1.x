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

package com.sun.ws.rest.impl.dispatch;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.dispatch.DispatcherProvider;
import com.sun.ws.rest.spi.service.ServiceFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class DispatcherFactory {
    
    private DispatcherFactory() {
    }
    
    /**
     * Create an array dispatchers from a resource.
     * 
     * @param resource the resource for which dispatchers should be created
     * @param config the the resource configuration.
     * @return the array of dispatchers for the resource.
     */
    public static URITemplateDispatcher[] createDispatchers(Class resource, ResourceConfig config) throws ContainerException {
        List<URITemplateDispatcher> l = new ArrayList<URITemplateDispatcher>();
        
        for (DispatcherProvider vp : ServiceFinder.find(DispatcherProvider.class)) {
            URITemplateDispatcher[] ds = vp.createDispatchers(resource, config);
            if (ds != null)
                Collections.addAll(l, ds);
        }
        
        return l.toArray(new URITemplateDispatcher[0]);
    }    
}
