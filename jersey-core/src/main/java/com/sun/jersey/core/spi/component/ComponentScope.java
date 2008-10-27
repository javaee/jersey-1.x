package com.sun.jersey.core.spi.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The scope contract for a component.
 */
public enum ComponentScope {

    /**
     * Declares the scope of the component is a singleton whose instance
     * is valid for the scope of the running application.
     */
    Singleton,

    /**
     * Declares that the scope of the component is per request whose instance
     * is valid for the scope of the current HTTP request and response.
     */
    PerRequest,

    /**
     * Declares that the scope of the component is undefined.
     */
    Undefined;

    /**
     * A immutable list comprising of the scopes Undefined and
     * Singleton, in that order.
     */
    public static final List<ComponentScope> UNDEFINED_SINGLETON =
            Collections.unmodifiableList(Arrays.asList(ComponentScope.Undefined, ComponentScope.Singleton));

    /**
     * A immutable list comprising of the scopes PerRequest, Undefined and
     * Singleton, in that order.
     */
    public static final List<ComponentScope> PERREQUEST_UNDEFINED_SINGLETON =
            Collections.unmodifiableList(Arrays.asList(ComponentScope.PerRequest, ComponentScope.Undefined, ComponentScope.Singleton));

    /**
     * A immutable list comprising of the scopes PerRequest and
     * Undefined, in that order.
     */
    public static final List<ComponentScope> PERREQUEST_UNDEFINED =
            Collections.unmodifiableList(Arrays.asList(ComponentScope.PerRequest, ComponentScope.Undefined));
}
