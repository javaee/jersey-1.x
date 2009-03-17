/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.server.impl.application;

import com.sun.jersey.spi.CloseableService;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;

/**
 *
 * @author paulsandoz
 */
public class CloseableServiceFactory implements
        InjectableProvider<Context, Type>, Injectable<CloseableService>, CloseableService {
    private static final Logger LOGGER = Logger.getLogger(CloseableServiceFactory.class.getName());

    private final HttpContext context;

    public CloseableServiceFactory(@Context HttpContext context) {
        this.context = context;
    }

    // InjectableProvider

    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }

    public Injectable getInjectable(ComponentContext ic, Context a, Type c) {
        if (c != CloseableService.class)
           return null;

        return this;
    }

    // Injectable

    public CloseableService getValue() {
        return this;
    }

    // CloseableService
    
    public void add(Closeable c) {
        Set<Closeable> s = (Set<Closeable>)context.getProperties().
                get(CloseableServiceFactory.class.getName());
        if (s == null) {
            s = new HashSet<Closeable>();
            context.getProperties().put(CloseableServiceFactory.class.getName(), s);
        }

        s.add(c);
    }

    public void close(HttpContext context) {
        Set<Closeable> s = (Set<Closeable>)context.getProperties().
                get(CloseableServiceFactory.class.getName());
        if (s != null) {
            for (Closeable c : s) {
                try {
                    c.close();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Unable to close", ex);
                }
            }
        }
    }
}