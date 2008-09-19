/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.spi.spring.container.servlet;

import java.lang.annotation.Annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.impl.container.servlet.PerSessionProvider;
import com.sun.jersey.impl.resource.PerRequestProvider;
import com.sun.jersey.impl.resource.SingletonProvider;
import com.sun.jersey.spi.resource.ResourceProvider;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 * <p>
 * This {@link ResourceProvider} is configured as the DefaultResourceProviderClass
 * (see {@link ResourceConfig#PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS}) by the
 * {@link SpringServlet} if there's not already another DefaultResourceProviderClass is
 * configured and if spring 2.5+ is used (determined by the availability of
 * the {@link Component} annotation).
 * </p>
 * <p>
 * For resource classes annotated with {@link Component}, this {@link ResourceProvider}
 * uses the value of the {@link Scope} annotation (provided by spring >= 2.5) on the
 * resource class. According to the value of the {@link Scope} annotation it creates
 * an appropriate {@link ResourceProvider} implementation and delegates all further work to this.
 * </p>
 * <p>
 * These spring scopes are supported: {@link SupportedSpringScopes}.
 * </p>
 * <p>
 * This {@link ResourceProvider} depends on spring 2.5
 * </p>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 */
public class SpringResourceProvider implements ResourceProvider {

    private static final Log LOG = LogFactory.getLog(SpringResourceProvider.class);

    /**
     * This enum defines the mapping of spring scopes to the jersey lifecycle/scopes
     */
    public enum SupportedSpringScopes {

        /**
         * Maps the spring scope "singleton" to the {@link SingletonProvider}
         */
        SINGLETON("singleton", SingletonProvider.class),
        /**
         * Maps the spring scope "prototype" to the {@link PerRequestProvider}
         */
        PROTOTYPE("prototype", PerRequestProvider.class),
        /**
         * Maps the spring scope "request" to the {@link PerRequestProvider}
         */
        REQUEST("request", PerRequestProvider.class),
        /**
         * Maps the spring scope "session" to the {@link PerSessionProvider}
         */
        SESSION("session", PerSessionProvider.class);
        private final String _springScope;
        private final Class<? extends ResourceProvider> _resourceProviderClass;

        private SupportedSpringScopes(String springScope,
                Class<? extends ResourceProvider> resourceProviderClass) {
            _springScope = springScope;
            _resourceProviderClass = resourceProviderClass;
        }

        public Class<? extends ResourceProvider> createResourceProviderClass() {
            return _resourceProviderClass;
        }

        public String getSpringScope() {
            return _springScope;
        }
        
        public static SupportedSpringScopes defaultSpringScope() {
            return SINGLETON;
        }

        /**
         * Selects the matching SupportedSpringScopes item or <code>null</code>.
         * @param springScope the spring scope
         * @return the matching SupportedSpringScopes item or <code>null</code>.
         */
        public static SupportedSpringScopes valueOfSpringScope(String springScope) {
            for (SupportedSpringScopes scope : values()) {
                if (scope.getSpringScope().equals(springScope)) {
                    return scope;
                }
            }
            return null;
        }

        public static String getSpringScopesAsCSV() {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values().length; i++) {
                final SupportedSpringScopes item = values()[i];
                sb.append(item.getSpringScope());
                if (i < values().length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }
    private ResourceProvider _resourceProvider;

    public void init(ComponentProvider provider, ComponentProvider resourceProvider, AbstractResource resource) {
        final Class<?> resourceClass = resource.getResourceClass();
        final Class<? extends ResourceProvider> resourceProviderClass;
        /* This resource provider is only responsible for spring beans
         * annotated with @Component, for all other resource classes
         * the default resource provider is used (see ResourceProviderFactory#createProvider)
         */
        if (!isAutodetectedSpringComponent( resourceClass )) {
            resourceProviderClass = PerRequestProvider.class;
        } else {
            /* This resource provider is only used if the resource class
             * does not have any jersey lifecycle annotation
             * -> we do not have to check this
             * -> if no supported spring scope is used we cannot handle this
             */
            final Scope scope = resourceClass.getAnnotation(Scope.class);
            if (scope != null) {
                final SupportedSpringScopes springScope = SupportedSpringScopes.valueOfSpringScope(scope.value());
                if (springScope != null) {
                    resourceProviderClass = springScope.createResourceProviderClass();
                } else {
                    throw new RuntimeException("No jersey lifecycle annotation specified on" +
                            " resource class " + resourceClass.getName() +
                            " and also no valid spring scope (valid scopes: " + SupportedSpringScopes.getSpringScopesAsCSV() + ")");
                }
            } else {
                resourceProviderClass = SupportedSpringScopes.defaultSpringScope().createResourceProviderClass();
            }
        }
        try {
            _resourceProvider = (ResourceProvider) provider.getInstance(
                    ComponentProvider.Scope.PerRequest, resourceProviderClass);
            _resourceProvider.init(provider, resourceProvider, resource);
        } catch (RuntimeException e) {
            LOG.error("Could not initialize resource provider for resource class " + resourceClass.getName());
            throw e;
        } catch (Exception e) {
            LOG.error("Could not initialize resource provider for resource class " + resourceClass.getName());
            e.printStackTrace();
            throw new RuntimeException("Could not initialize resource provider for resource class ", e);
        }
    }
    
    /**
     * Determines, if the given class is an autodetected spring component as described
     * in http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-stereotype-annotations.
     * 
     * @param resourceClass the resource class
     * @return true if this class is annotated with {@link @Component} or with some other annotation
     *          that itself is annotated with {@link @Component} (like e.g. {@link @Controller},
     *          {@link @Service} or {@link @Repository}).
     */
    private boolean isAutodetectedSpringComponent( final Class<?> resourceClass ) {
        
        final Annotation[] annotations = resourceClass.getAnnotations();
        if ( annotations != null ) {
            for ( Annotation annotation : annotations ) {
                /* spring's specialized component annotations like
                 * @Service, @Controller or @Repository are all annotated
                 * with @Component, so we can check this. 
                 */
                final Class<? extends Annotation> annotationType = annotation.annotationType();
                if ( annotationType.equals( Component.class )
                        || annotationType.getAnnotation( Component.class ) != null ) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public Object getInstance(ComponentProvider provider, HttpContext context) {
        return _resourceProvider.getInstance(provider, context);
    }
    
}
