/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.provider;

import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.VariantListBuilderImpl;
import com.sun.ws.rest.impl.uri.UriBuilderImpl;
import com.sun.ws.rest.spi.HeaderDelegateProvider;
import com.sun.ws.rest.spi.service.ServiceFinder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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
