/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.spi.inject;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A utility class that may implemented to support a per-request injectable 
 * provider for a specific type T.
 * 
 * @param <A> the annotation type
 * @param <T> the type returned by {@link Injectable#getValue}
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class PerRequestTypeInjectableProvider <A extends Annotation, T> 
    implements InjectableProvider<A, Type> {
    
    private final Type t;
    
    /**
     * Construct a new instance with the Type
     *
     * @param t the type of T.
     */
    public PerRequestTypeInjectableProvider(Type t) {
        this.t = t;
    }

    public final ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    public final Injectable getInjectable(ComponentContext ic, A a, Type c) {
        if (c.equals(t)) {
            return getInjectable(ic, a);
        } else
            return null;
    }

    /**
     * Get an injectable for type T.
     * 
     * @param ic the injectable context
     * @param a the annotation instance
     * @return an Injectable instance, otherwise null if an instance cannot
     *         be created.
     */
    public abstract Injectable<T> getInjectable(ComponentContext ic, A a);
}