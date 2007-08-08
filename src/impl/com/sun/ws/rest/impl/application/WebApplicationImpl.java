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

package com.sun.ws.rest.impl.application;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.ws.rs.WebApplicationException;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.ThreadLocalHttpContext;
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.RootResourceClass;
import com.sun.ws.rest.impl.response.Responses;
import com.sun.ws.rest.spi.resolver.WebResourceResolverFactory;
import com.sun.ws.rest.spi.container.ContainerRequest;
import com.sun.ws.rest.spi.container.ContainerResponse;
import com.sun.ws.rest.spi.container.WebApplication;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.PreconditionEvaluator;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * A Web application that contains a set of resources, each referenced by 
 * an absolute URI template.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class WebApplicationImpl implements WebApplication {
    boolean initiated;
    
    ResourceConfig resourceConfig;
    
    RootResourceClass rootResourceClass;

    Object containerMomento;

    final ThreadLocalHttpContext context;
    
    final HttpHeaders httpHeadersProxy;
    
    final UriInfo uriInfoProxy;
    
    final PreconditionEvaluator preconditionEvaluatorProxy;
    
    final Map<String, EntityManagerFactory> entityManagerCache;
    
    final Map<Class<?>, Injectable> injectables;
    
    public final List<URITemplateDispatcher> dispatchers = new ArrayList<URITemplateDispatcher>();
    
    private final Map<Class, ResourceClass> metaClassMap = new WeakHashMap<Class, ResourceClass>();

    public WebApplicationImpl() {
        this.context = new ThreadLocalHttpContext();
        this.entityManagerCache = new HashMap<String, EntityManagerFactory>();
        
        InvocationHandler i = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(context.getHttpRequestContext(), args);
            }
        };
        this.httpHeadersProxy = createProxy(HttpHeaders.class, i);
        this.uriInfoProxy = createProxy(UriInfo.class, i);
        this.preconditionEvaluatorProxy = createProxy(PreconditionEvaluator.class, i);
        
        this.injectables = createInjectables();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> c, InvocationHandler i) {
        return (T)Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            new Class[] { c },
            i);
    }
    
    public ResourceClass getResourceClass(Class c) {
        return rootResourceClass.getResourceClass(c);
    }

    // WebApplication
            
    public void initiate(Object containerMomento, ResourceConfig resourceConfig, 
            WebResourceResolverFactory resolverFactory) {
        if (resourceConfig == null)
            throw new IllegalArgumentException();
        
        if (initiated)
            throw new ContainerException("Web application is already initiated");
        
        this.initiated = true;
        this.resourceConfig = resourceConfig;
        this.rootResourceClass = new RootResourceClass(containerMomento, resourceConfig, resolverFactory);
        this.containerMomento = containerMomento;
    }

    public void register(Class... resourceClasses) {
        Set<Class> resourceClassesSet = new HashSet<Class>(
                Arrays.asList(resourceClasses));
        register(resourceClassesSet);
    }
  
    public void register(Set<Class> resourceClasses) {
        // TODO, dynamically add resource classes to initiated web application
        throw new UnsupportedOperationException();
    }
    
    public void handleRequest(ContainerRequest request, ContainerResponse response) {
        final WebApplicationContext localContext = new WebApplicationContext(this, request, response);        
        context.set(localContext);
        
        if (resourceConfig.isRedirectToNormalizedURI()) {
            final URI uri = request.getURI();
            final URI normalizedUri = uri.normalize();            

            if (uri != normalizedUri) {
                response.setResponse(ResponseBuilderImpl.temporaryRedirect(normalizedUri).build());
                return;
            }
        }

        // TODO the matching algorithm currently works from an absolute path
        String path = "/" + request.getURIPath();

        if (resourceConfig.isIgnoreMatrixParams())
            // TODO check for annotation on resource
            // Need to support overriding functionality on resource        
            path = stripMatrixParams(path);

        try {
            if (!rootResourceClass.dispatch(localContext, path)) {
                // Resource was not found
                response.setResponse(Responses.NOT_FOUND);
            }
        } catch (WebApplicationException e) {
            onExceptionWithWebApplication(e, localContext.response);
        }
    }
    
    /**
     * Strip the matrix parameters from a path
     */
    private String stripMatrixParams(String path) {        
        int e = path.indexOf(';');
        if (e == -1)
            return path;

        int s = 0;
        StringBuilder sb = new StringBuilder();
        do {
            // Append everything up to but not including the ';'
            sb.append(path, s, e);

            // Skip everything up to but not including the '/'
            s = path.indexOf('/', e + 1);
            if (s == -1) {
                break;
            }
            e = path.indexOf(';', s);
        } while(e != -1);
        
        if (s != -1) {
            // Append any remaining characters
            sb.append(path, s, path.length());
        }
        
        return sb.toString();
    }  
    
    private abstract class Injectable<T extends Annotation, V> {
        abstract Class<T> getAnnotationClass();
        
        void inject(Object resource, Field f) {
            if (getFieldValue(resource, f) != null) {
                // skip fields that already have a value 
                // (may have been injected by the container impl)
                return; 
            }
            
            T a = f.getAnnotation(getAnnotationClass());
            if (a == null) {
                // skip if the annotation is not declared
                return;
            }
            
            V value = getInjectableValue(a);
            if (value != null)
                setFieldValue(resource, f, value);
        }
        
        abstract V getInjectableValue(T a);
        
        private void setFieldValue(final Object resource, final Field f, final Object value) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        f.set(resource, value);
                        return null;
                    } catch (IllegalAccessException e) {
                        throw new ContainerException(e);
                    }
                }
            });
        }

        private Object getFieldValue(final Object resource, final Field f) {
            return AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        return f.get(resource);
                    } catch (IllegalAccessException e) {
                        throw new ContainerException(e);
                    }
                }
            });
        }
    }
    
    private abstract class HttpContextInjectable<V> extends Injectable<HttpContext, V> {
        public Class<HttpContext> getAnnotationClass() {
            return HttpContext.class;
        }
    }
    
    private Map<Class<?>, Injectable> createInjectables() {
        Map<Class<?>, Injectable> injectables = new HashMap<Class<?>, Injectable>();
                
        injectables.put(HttpContextAccess.class,
                new HttpContextInjectable<HttpContextAccess>() {
                    public HttpContextAccess getInjectableValue(HttpContext c) {
                        return context;
                    }
                }
            );
        
        injectables.put(HttpHeaders.class,
                new HttpContextInjectable<HttpHeaders>() {
                    public HttpHeaders getInjectableValue(HttpContext c) {
                        return httpHeadersProxy;
                    }
                }
            );
            
        injectables.put(UriInfo.class,
                new HttpContextInjectable<UriInfo>() {
                    public UriInfo getInjectableValue(HttpContext c) {
                        return uriInfoProxy;
                    }
                }
            );
            
        injectables.put(PreconditionEvaluator.class,
                new HttpContextInjectable<PreconditionEvaluator>() {
                    public PreconditionEvaluator getInjectableValue(HttpContext c) {
                        return preconditionEvaluatorProxy;
                    }
                }
            );
            
        injectables.put(EntityManagerFactory.class,
                new Injectable<PersistenceUnit, EntityManagerFactory>() {
                    public Class<PersistenceUnit> getAnnotationClass() {
                        return PersistenceUnit.class;
                    }
                    
                    public EntityManagerFactory getInjectableValue(PersistenceUnit pu) {
                        return getEntityManagerFactory(pu.unitName());
                    }
                }
            );

        return injectables;
    }


    /**
     * Inject resources on a Web resource.
     * @param resourceClass the class of the resource
     * @param resource the resource instance
     */
    /* package */ void injectResources(Object resource) {
        Class resourceClass = resource.getClass();
        for (Field f : resourceClass.getDeclaredFields()) {
            
            Injectable i = injectables.get(f.getType());
            if (i != null)
                i.inject(resource, f);
        }
    }
    
    synchronized EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        EntityManagerFactory emf;
        if (entityManagerCache.containsKey(persistenceUnit)) {
            emf = entityManagerCache.get(persistenceUnit);
        } else {
            emf = Persistence.createEntityManagerFactory(persistenceUnit);
            entityManagerCache.put(persistenceUnit, emf);
        }
        return emf;
    }

    public static void onExceptionWithWebApplication(WebApplicationException e, HttpResponseContext response) {
        // Log the stack trace
        e.printStackTrace();

        Response r = e.getResponse();
        if (r.getStatus() >= 400 && r.getEntity() == null) {
            // Write out the exception to a string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();

            r = new ResponseBuilderImpl(r).entity(sw.toString()).type("text/plain").build();
        }
        response.setResponse(r);
    }
}
