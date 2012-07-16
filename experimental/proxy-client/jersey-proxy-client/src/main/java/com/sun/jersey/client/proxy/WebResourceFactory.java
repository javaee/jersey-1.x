/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.client.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

/**
 * Factory for client-side representation of a resource.
 * See the <a href="package-summary.html">package overview</a>
 * for an example on how to use this class.
 *
 * @author Martin Matula (martin.matula at oracle.com)
 */
public final class WebResourceFactory implements InvocationHandler {
    private final WebResource resource;
    private final UriBuilder uriBuilder;
    private final MultivaluedMap<String, Object> headers;
    private final Map<String, Cookie> cookies;
    private final Map<String, Object> pathParams;
    private final Form form;

    /**
     * Creates a new client-side representation of a resource described by
     * the interface passed in the first argument.
     *
     * Calling this method has the same effect as calling {@code WebResourceFactory.newResource(resourceInterface, rootTarget, false)}.
     *
     * @param <C> Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param resource WebResource pointing to the resource or the parent of the resource.
     * @return Instance of a class implementing the resource interface that can
     * be used for making requests to the server.
     */
    public static <C> C newResource(Class<C> resourceInterface, WebResource resource) {
        return newResource(resourceInterface, resource, null, new MultivaluedHashMap<String, Object>(),
                new HashMap<String, Cookie>(), new HashMap<String, Object>(), new Form());
    }

    /**
     * Creates a new client-side representation of a resource described by
     * the interface passed in the first argument.
     *
     * @param <C> Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param resource WebResource pointing to the resource or the parent of the resource.
     * @param uriBuilder Uri builder holding the URI path of the resource to be created (if {@code null}, URI from the
     *                   provided web resource appended by path annotation on this resource will be used).
     * @return Instance of a class implementing the resource interface that can
     * be used for making requests to the server.
     */
    @SuppressWarnings("unchecked")
    static <C> C newResource(Class<C> resourceInterface, WebResource resource, UriBuilder uriBuilder,
                             MultivaluedMap<String, Object> headers, Map<String, Cookie> cookies,
                             Map<String, Object> pathParams, Form form) {
        return (C) Proxy.newProxyInstance(resourceInterface.getClassLoader(),
                new Class[] {resourceInterface}, new WebResourceFactory(resource, uriBuilder != null ? uriBuilder :
                    addPathFromAnnotation(resourceInterface, resource.getUriBuilder()),
                headers, cookies, pathParams, form));
    }

    private WebResourceFactory(WebResource resource, UriBuilder uriBuilder,
                               MultivaluedMap<String, Object> headers, Map<String, Cookie> cookies,
                               Map<String, Object> pathParams, Form form) {
        this.resource = resource;
        this.uriBuilder = uriBuilder == null ? resource.getUriBuilder() : uriBuilder;
        this.cookies = cookies;
        this.form = form;
        this.headers = headers;
        this.pathParams = pathParams;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // get the interface describing the resource
        final Class<?> proxyIfc = proxy.getClass().getInterfaces()[0];

        // response type
        Class<?> responseType = method.getReturnType();

        // determine method name
        String httpMethod = getHttpMethodName(method);
        if (httpMethod == null) {
            for (Annotation ann : method.getAnnotations()) {
                httpMethod = getHttpMethodName(ann.annotationType());
                if (httpMethod != null) {
                    break;
                }
            }
        }

        UriBuilder backupBuilder = uriBuilder.clone();

        // create a new UriBuilder appending the @Path attached to the method
        UriBuilder uriBuilder = addPathFromAnnotation(method, backupBuilder);

        if (httpMethod == null) {
            if (uriBuilder == null) {
                // no path annotation on the method -> fail
                throw new UnsupportedOperationException("Not a resource method.");
            } else if (!responseType.isInterface()) {
                // the method is a subresource locator, but returns class,
                // not interface - can't help here
                throw new UnsupportedOperationException("Return type not an interface");
            }
        }

        if (uriBuilder == null) {
            uriBuilder = backupBuilder;
        }

        // process method params (build maps of (Path|Form|Cookie|Matrix|Header..)Params
        // and extract entity type
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>(this.headers);
        Map<String, Object> pathParams = new HashMap<String, Object>(this.pathParams);
        Map<String, Cookie> cookies = new HashMap<String, Cookie>(this.cookies);
        Form form = new Form();
        form.putAll(this.form);
        Annotation[][] paramAnns = method.getParameterAnnotations();
        Object entity = null;
        Type entityType = null;
        for (int i = 0; i < paramAnns.length; i++) {
            Map<Class, Annotation> anns = new HashMap<Class, Annotation>();
            for (Annotation ann : paramAnns[i]) {
                anns.put(ann.annotationType(), ann);
            }
            Annotation ann;
            Object value = args[i];
            if (anns.isEmpty()) {
                entityType = method.getGenericParameterTypes()[i];
                entity = value;
            } else {
                if (value == null && (ann = anns.get(DefaultValue.class)) != null) {
                    value = ((DefaultValue) ann).value();
                }
                if (value != null) {
                    if ((ann = anns.get(PathParam.class)) != null) {
                        pathParams.put(((PathParam) ann).value(), value);
                    } else if ((ann = anns.get((QueryParam.class))) != null) {
                        uriBuilder.queryParam(((QueryParam) ann).value(), value);
                    } else if ((ann = anns.get((HeaderParam.class))) != null) {
                        headers.add(((HeaderParam) ann).value(), value);
                    } else if ((ann = anns.get((CookieParam.class))) != null) {
                        String name = ((CookieParam) ann).value();
                        Cookie c;
                        if (!(value instanceof Cookie)) {
                            c = new Cookie(name, value.toString());
                        } else {
                            c = (Cookie) value;
                            if (!name.equals(((Cookie) value).getName())) {
                                // is this the right thing to do? or should I fail? or ignore the difference?
                                c = new Cookie(name, c.getValue(), c.getPath(), c.getDomain(), c.getVersion());
                            }
                        }
                        cookies.put(name, c);
                    } else if ((ann = anns.get((MatrixParam.class))) != null) {
                        uriBuilder.matrixParam(((MatrixParam) ann).value(), value);
                    } else if ((ann = anns.get((FormParam.class))) != null) {
                        form.add(((FormParam) ann).value(), value.toString());
                    }
                }
            }
        }

        if (httpMethod == null) {
            // the method is a subresource locator
            return WebResourceFactory.newResource(responseType, resource, uriBuilder, headers, cookies, pathParams, form);
        }

        // accepted media types
        Produces produces = method.getAnnotation(Produces.class);
        if (produces == null) {
            produces = proxyIfc.getAnnotation(Produces.class);
        }
        String[] accepts = produces == null ? null : produces.value();

        // determine content type
        String contentType = null;
        if (entity != null) {
            Consumes consumes = method.getAnnotation(Consumes.class);
            if (consumes == null) {
                consumes = proxyIfc.getAnnotation(Consumes.class);
            }
            if (consumes != null && consumes.value().length > 0) {
                // TODO: should consider q/qs instead of picking the first one
                contentType = consumes.value()[0];
            }
        }

        WebResource.Builder b = resource.uri(uriBuilder.buildFromMap(pathParams)).getRequestBuilder();

        // apply header params and cookies
        for (Cookie c : cookies.values()) {
            b.cookie(c);
        }

        for (Map.Entry<String, List<Object>> header : headers.entrySet()) {
            for (Object value : header.getValue()) {
                b = b.header(header.getKey(), value);
            }
        }

        if (accepts != null) {
            b.accept(accepts);
        }

        Object result;

        if (entity == null && !form.isEmpty()) {
            entity = form;
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            if (!form.isEmpty()) {
                if (entity instanceof Form) {
                    ((Form) entity).putAll(form);
                } else {
                    // TODO: should at least log some warning here
                }
            }
        }

        final GenericType responseGenericType;
        if (Response.class.isAssignableFrom(responseType)) {
            responseType = ClientResponse.class;
            responseGenericType = new GenericType(ClientResponse.class);
        } else {
            responseGenericType = new GenericType(method.getGenericReturnType());
        }
        if (entity != null) {
            if (entityType instanceof ParameterizedType) {
                entity = new GenericEntity(entity, entityType);
            }
            result = b.type(contentType).method(httpMethod, responseGenericType, entity);
        } else {
            result = b.method(httpMethod, responseGenericType);
        }

        if (responseType == ClientResponse.class) {
            result = new ClientResponseProxy((ClientResponse) result);
        }

        return result;
    }

    private static UriBuilder addPathFromAnnotation(AnnotatedElement ae, UriBuilder uriBuilder) {
        Path p = ae.getAnnotation(Path.class);
        if (p != null) {
            return uriBuilder.path(p.value());
        }
        return null;
    }

    private static String getHttpMethodName(AnnotatedElement ae) {
        HttpMethod a = ae.getAnnotation(HttpMethod.class);
        return a == null ? null : a.value();
    }
}
