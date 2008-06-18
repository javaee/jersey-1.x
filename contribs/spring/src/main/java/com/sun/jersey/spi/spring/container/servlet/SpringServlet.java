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
package com.sun.jersey.spi.spring.container.servlet;

import com.sun.jersey.spi.service.ComponentContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Apr 3, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class SpringServlet extends ServletContainer {

    private static final long serialVersionUID = 5686655395749077671L;
    
    private static final Log LOG = LogFactory.getLog( SpringServlet.class );

    public static class SpringComponentProvider implements ComponentProvider {
        
        /* (non-Javadoc)
         * @see com.sun.ws.rest.spi.service.ComponentProvider#getInjectableInstance(java.lang.Object)
         */
        public <T> T getInjectableInstance( T instance ) {
            if ( AopUtils.isAopProxy( instance ) ) {
                final Advised aopResource = (Advised)instance;
                try {
                    @SuppressWarnings("unchecked")
                    final T result = (T) aopResource.getTargetSource().getTarget();
                    return result;
                } catch ( Exception e ) {
                    LOG.fatal( "Could not get target object from proxy.", e );
                    throw new RuntimeException( "Could not get target object from proxy.", e );
                }
            }
            else {
                return instance;
            }
        }

        private ConfigurableApplicationContext springContext;

        public SpringComponentProvider(ConfigurableApplicationContext springContext) {
            this.springContext = springContext;
        }
        
        public <T> T getInstance( Scope scope, Class<T> clazz ) 
                throws InstantiationException, IllegalAccessException {
            return getInstance( null, scope, clazz );
        }

        public <T> T getInstance(Scope scope, Constructor<T> constructor, 
                Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, 
                IllegalAccessException, InvocationTargetException {
            
            return getInstance( null, scope, constructor.getDeclaringClass() );
            
        }

        public <T> T getInstance( ComponentContext cc, Scope scope, Class<T> clazz ) 
                throws InstantiationException, IllegalAccessException {

            
            final Autowire autowire = clazz.getAnnotation( Autowire.class );
            if ( autowire != null ) {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Creating resource class "+ clazz.getSimpleName() +" annotated with @"+ Autowire.class.getSimpleName() +" as spring bean." );
                }
                /* use createBean to have a fully initialized bean, including
                 * applied BeanPostProcessors (in contrast to #autowire()).
                 */
                final Object result = springContext.getBeanFactory().createBean( clazz,
                        autowire.mode().getSpringCode(), autowire.dependencyCheck() );
                return clazz.cast( result );
            }
            
            final String beanName = getBeanName( cc, clazz, springContext );
            if ( beanName == null ) {
                return null;
            }
            
            /* if the scope is null, this means that jersey simply doesn't know what's
             * the scope of this dependency, so it's left to the application...
             */
            if ( scope == Scope.Undefined
                    || scope == Scope.Singleton && springContext.isSingleton(beanName)
                    || scope == Scope.PerRequest && springContext.isPrototype( beanName ) ) {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Retrieving bean '"+ beanName +"' for resource class "+ clazz.getSimpleName() +" from spring." );
                }
                final Object result = springContext.getBean( beanName, clazz );
                return clazz.cast( result );
            }
            else {
                return null;
            }
            
        }
        
        public void inject(Object instance) {
        }

    };
    
    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        try {
            final WebApplicationContext springContext = WebApplicationContextUtils.
                    getRequiredWebApplicationContext(getServletContext());
            
            wa.initiate(rc, new SpringComponentProvider((ConfigurableApplicationContext) springContext));
        } catch( RuntimeException e ) {
            LOG.error( "Got exception while trying to initialize", e );
            throw e;
        }
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service( ServletRequest req, ServletResponse res ) throws ServletException, IOException {
        LOG.debug( "Starting..." );
        try {
            super.service( req, res );
        } catch( RuntimeException e ) {
            LOG.error( "Caught exception.", e );
        }
        LOG.debug( "Finished." );
    }
    
    private static String getBeanName( ComponentContext cc, Class<?> c, ApplicationContext springContext ) {

        boolean annotatedWithInject = false;
        if ( cc != null ) {
            final Inject inject = getAnnotation( cc.getAnnotations(), Inject.class );
            if ( inject != null ) {
                annotatedWithInject = true;
                if ( inject.value() != null && !inject.value().equals( "" ) ) {
                    return inject.value();
                }
                
            }
        }
        
        final String names[] = springContext.getBeanNamesForType( c );
        
        if (names.length == 0) {
            return null;
        }
        else if ( names.length == 1 ) {
            return names[0];
        }
        else {
            
            final StringBuilder sb = new StringBuilder();
            sb.append( "There are multiple beans configured in spring for the type " ).append( c.getName() ).append( "." );
            

            if ( annotatedWithInject ) {
                sb.append( "\nYou should specify the name of the preferred bean at @Inject: Inject(\"yourBean\")." );
            }
            else {
                sb.append( "\nAnnotation information was not available, the reason might be because you're not using " +
                "@Inject. You should use @Inject and specifiy the bean name via Inject(\"yourBean\")." );
            }
            
            sb.append( "Available bean names: " ).append( toCSV( names ) );

            throw new RuntimeException( sb.toString() );
        }
    }

    private static <T extends Annotation> T getAnnotation( Annotation[] annotations,
            Class<T> clazz ) {
        if ( annotations != null ) {
            for ( Annotation annotation : annotations ) {
                if ( annotation.annotationType().equals( clazz ) ) {
                    return clazz.cast( annotation );
                }
            }
        }
        return null;
    }

    static <T> String toCSV( T[] items ) {
        if ( items == null ) {
            return null;
        }
        return toCSV( Arrays.asList( items ) );
    }
    
    static <I> String toCSV( Collection<I> items ) {
        return toCSV( items, ", ", null );
    }
    
    static <I> String toCSV( Collection<I> items, String separator, String delimiter ) {
        if ( items == null ) {
            return null;
        }
        if ( items.isEmpty() ) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for ( final Iterator<I> iter = items.iterator(); iter.hasNext(); ) {
            if ( delimiter != null ) {
                sb.append( delimiter );
            }
            final I item = iter.next();
            sb.append( item );
            if ( delimiter != null ) {
                sb.append( delimiter );
            }
            if ( iter.hasNext() ) {
                sb.append( separator );
            }
        }
        return sb.toString();
    }
 
}
