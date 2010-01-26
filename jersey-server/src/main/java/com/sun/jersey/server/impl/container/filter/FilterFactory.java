/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.server.impl.container.filter;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class FilterFactory {
    private static final Logger LOGGER = Logger.getLogger(FilterFactory.class.getName());
    
    private final ProviderServices providerServices;

    private final List<ContainerRequestFilter> requestFilters = new LinkedList<ContainerRequestFilter>();

    private final List<ContainerResponseFilter> responseFilters = new LinkedList<ContainerResponseFilter>();

    private final List<ResourceFilterFactory> resourceFilterFactories = new LinkedList<ResourceFilterFactory>();

    public FilterFactory(ProviderServices providerServices) {
        this.providerServices = providerServices;
    }

    public void init(ResourceConfig resourceConfig) {
        // Initiate request filters
        requestFilters.addAll(getRequestFilters(
                resourceConfig.getProperty(
                    ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS)));
        requestFilters.addAll(providerServices.getServices(ContainerRequestFilter.class));

        // Initiate response filters
        responseFilters.addAll(getResponseFilters(
                resourceConfig.getProperty(
                    ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS)));
        responseFilters.addAll(providerServices.getServices(ContainerResponseFilter.class));

        // Initiate resource filter factories
        resourceFilterFactories.addAll(getResourceFilterFactories(
                resourceConfig.getProperty(
                    ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES)));
        resourceFilterFactories.addAll(providerServices.getServices(ResourceFilterFactory.class));
        resourceFilterFactories.add(new AnnotationResourceFilterFactory(this));
    }

    public List<ContainerRequestFilter> getRequestFilters() {
        return requestFilters;
    }

    public List<ContainerResponseFilter> getResponseFilters() {
        return responseFilters;
    }

    public List<ResourceFilter> getResourceFilters(AbstractMethod am) {
        List<ResourceFilter> resourceFilters = new LinkedList<ResourceFilter>();
        for (ResourceFilterFactory rff : resourceFilterFactories) {
            List<ResourceFilter> rfs = rff.create(am);
            if (rfs != null)
                resourceFilters.addAll(rfs);
        }
        return resourceFilters;
    }

    public List<ResourceFilter> getResourceFilters(Class<? extends ResourceFilter>[] classes) {
        if (classes == null || classes.length == 0)
            return Collections.EMPTY_LIST;

        return providerServices.getInstances(ResourceFilter.class, classes);
    }

    public List<ContainerRequestFilter> getRequestFilters(List<ResourceFilter> resourceFilters) {
        final List<ContainerRequestFilter> filters = new LinkedList<ContainerRequestFilter>();
        for (ResourceFilter rf : resourceFilters) {
            ContainerRequestFilter crf = rf.getRequestFilter();
            if (crf != null)
                filters.add(crf);
        }
        return filters;
    }

    public List<ContainerResponseFilter> getResponseFilters(List<ResourceFilter> resourceFilters) {
        final List<ContainerResponseFilter> filters = new LinkedList<ContainerResponseFilter>();
        for (ResourceFilter rf : resourceFilters) {
            ContainerResponseFilter crf = rf.getResponseFilter();
            if (crf != null)
                filters.add(crf);
        }
        return filters;
    }

    private List<ContainerRequestFilter> getRequestFilters(Object o) {
        return getFilters(ContainerRequestFilter.class, o);
    }
    
    private List<ContainerResponseFilter> getResponseFilters(Object o) {
        return getFilters(ContainerResponseFilter.class, o);        
    }

    private List<ResourceFilterFactory> getResourceFilterFactories(Object o) {
        return getFilters(ResourceFilterFactory.class, o);
    }

    private <T> List<T> getFilters(Class<T> c, Object o) {
        if (o == null)
            return Collections.emptyList();
        
        if (o instanceof String) {
            return getFilters(c, 
                    DefaultResourceConfig.getElements(new String[] {(String)o}));
        } else if (o instanceof String[]) {
            return getFilters(c, 
                    DefaultResourceConfig.getElements((String[])o));            
        } else if (o instanceof List) {
            return getFilters(c, (List)o);
        } else {
            LOGGER.severe("The filters, " + 
                    o.getClass().getName() + 
                    " declared for " + 
                    c.getName() + 
                    "MUST be of the type String[], String or List");
            return Collections.emptyList();
        }
    }
    
    private <T> List<T> getFilters(Class<T> c, String[] classNames) {
        List<T> f = new LinkedList<T>();
        f.addAll(providerServices.getInstances(c, classNames));
        return f;
    }
    
    private <T> List<T> getFilters(Class<T> c, List<?> l) {
        List<T> f = new LinkedList<T>();
        for (Object o : l) {
            if (o instanceof String) {
                f.addAll(providerServices.getInstances(c,
                        DefaultResourceConfig.getElements(new String[] {(String)o})));
            } else if (o instanceof String[]) {
                f.addAll(providerServices.getInstances(c,
                        DefaultResourceConfig.getElements((String[])o)));
            } else if (c.isInstance(o)) {
                f.add(c.cast(o));
            } else if (o instanceof Class) {
                Class fc = (Class)o;
                if (c.isAssignableFrom(fc)) {
                    f.addAll(providerServices.getInstances(c, new Class[] {fc}));
                } else {
                    LOGGER.severe("The filter, of type" +
                            o.getClass().getName() +
                            ", MUST be of the type Class<? extends" + c.getName() + ">" +
                            ". The filter is ignored.");
                }
            } else {
                LOGGER.severe("The filter, of type" +
                        o.getClass().getName() +
                        ", MUST be of the type String, String[], Class<? extends " + c.getName() + ">, or an instance of " + c.getName() +
                        ". The filter is ignored.");
            }            
        }
        providerServices.getComponentProviderFactory().injectOnProviderInstances(f);
        return f;
    }
}