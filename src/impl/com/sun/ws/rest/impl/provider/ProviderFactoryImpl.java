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

package com.sun.ws.rest.impl.provider;

import com.sun.ws.rest.spi.service.ServiceFinder;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ProviderFactoryImpl extends ProviderFactory {
    
    public <T> T createInstance(Class<T> type) {
        for (T t : ServiceFinder.find(type)) {
            return t;
        }     
        
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> HeaderProvider<T> createHeaderProvider(Class<T> type) {
        // This is obviously slow
        // Caching the providers using a Map with key of type and value of provider
        // for previously created resources will be faster
        for (HeaderProvider<T> tsp : ServiceFinder.find(HeaderProvider.class, true)) {
            if (tsp.supports(type))
                return tsp;
        }     
        
        throw new IllegalArgumentException("A header provider for type, " + type + ", is not supported");
    }
    
    @SuppressWarnings("unchecked")
    public <T> EntityProvider<T> createEntityProvider(Class<T> type) {
        // This is obviously slow
        // Caching the providers using a Map with key of type and value of provider
        // for previously created resources will be faster
        for (EntityProvider<T> tsp : ServiceFinder.find(EntityProvider.class, true)) {
            if (tsp.supports(type))
                return tsp;
        }     
        
        throw new IllegalArgumentException("A entity provider for type, " + type + ", is not supported");
    }

}
