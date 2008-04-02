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

import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.core.ApplicationConfigAdapter;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.VariantListBuilderImpl;
import com.sun.ws.rest.impl.uri.UriBuilderImpl;
import com.sun.ws.rest.spi.HeaderDelegateProvider;
import com.sun.ws.rest.spi.service.ServiceFinder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;


/**
 *
 * @author ps23762
 */
public class RuntimeDelegateImpl extends RuntimeDelegate {

    @Override
    public UriBuilder createUriBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public ResponseBuilder createResponseBuilder() {
        return new ResponseBuilderImpl();
    }

    @Override
    public VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilderImpl();
    }
    
    @Override
    public <T> T createEndpoint(ApplicationConfig applicationConfig, 
            Class<T> endpointType) 
            throws IllegalArgumentException, UnsupportedOperationException {
        return ContainerFactory.createContainer(endpointType, 
                new ApplicationConfigAdapter(applicationConfig));
    }
    
    private AtomicReference<Set<HeaderDelegateProvider>> atomicHeaderDelegates = 
            new AtomicReference<Set<HeaderDelegateProvider>>();
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        Set<HeaderDelegateProvider> headerDelegates = atomicHeaderDelegates.get();
        if (headerDelegates == null) {
            headerDelegates = cacheProviderList(atomicHeaderDelegates, 
                    HeaderDelegateProvider.class);
        }
        
        for (HeaderDelegateProvider p: headerDelegates) 
            if (p.supports(type))
                return p;

        throw new IllegalArgumentException("A header delegate provider for type, " + type + 
                ", is not supported");
    }
    
    private <T> Set<T> cacheProviderList(AtomicReference<Set<T>> atomicSet, 
            Class<T> c) {
        synchronized(atomicSet) {
            Set<T> s = atomicSet.get();
            if (s == null) {
                s = new HashSet<T>();
                for (T p : ServiceFinder.find(c, true)) {
                    s.add(p);
                }     
                atomicSet.set(s);
            }
            return s;
        }
    }
}