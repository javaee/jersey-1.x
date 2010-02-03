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

package com.sun.jersey.client.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.LinkHeader;
import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.Name;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.jvnet.ws.wadl.Param;
import org.jvnet.ws.wadl2java.ast.MethodNode;
import org.jvnet.ws.wadl2java.ast.RepresentationNode;
import org.jvnet.ws.wadl2java.ast.ResourceNode;

import static javax.ws.rs.core.MediaType.*;

/**
 * ControllerInvocationHandler class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class ControllerInvocationHandler<T> implements InvocationHandler {

    private Client client;

    private T model;

    private Class<?> modelClass;

    private Class<?> ctrlClass;

    private ClientResponse response;

    private Map<String, LinkHeader> actionHeaders;

    private Object entity;

    private MultivaluedMapImpl queryParams;

    private Map<String, MethodNode> wadlMetadataCache;

    public ControllerInvocationHandler(Client client, T instance,
            ClientResponse response, Class<?> ctrlClass)
    {
        // Check if annotation is present in controller
        if (!ctrlClass.isAnnotationPresent(HypermediaController.class)) {
            throw new IllegalArgumentException("Hypermedia controller " +
                    "class must have @HypermediaController annotation");
        }
        this.client = client;
        this.model = instance;
        this.response = response;
        this.modelClass = instance.getClass();
        this.ctrlClass = ctrlClass;

        actionHeaders = new HashMap<String, LinkHeader>();
        updateActionHeaders();
        queryParams = new MultivaluedMapImpl();
    }

    /**
     * Maps a method invocation in a controller interface to an
     * HTTP request on an action resource. Use WADL if not enough
     * static information is available for the mapping.
     */
    public Object invoke(Object o, Method method, Object[] params) 
            throws Throwable
    {
        // Clear entity reference and query values from previous call
        entity = null;
        queryParams.clear();

        // Trying to access local model (non-action method)
        Action action = method.getAnnotation(Action.class);
        if (action == null && method.getReturnType() == modelClass) {
            return model;
        }

        // Check if @Action annotation is present
        if (action == null) {
            throw new RuntimeException(
                    "Method " + method.getName() + " in "
                    + o.getClass().getInterfaces()[0].getName()
                    + " must have @Action or @Model annotation");
        }

        // Empty set of action headers?
        if (actionHeaders == null) {
            throw new RuntimeException(
                    "Action '" + action.value() + "' is not available"
                    + " in current context");
        }

        // Check if action is available in current context
        LinkHeader h = actionHeaders.get(action.value());
        if (h == null) {
             throw new RuntimeException(
                    "Action '" + action.value() + "' is not available"
                    + " in current context");
        }

        // If HTTP method annotation, must match 'op' in link header
        String httpMethod = getHttpMethod(ctrlClass, method);
        if (httpMethod == null) {
            httpMethod = h.getOp();
            assert httpMethod != null;
        } else if (!httpMethod.equals(h.getOp())) {
            throw new RuntimeException(
                    "HTTP method annotation " + httpMethod
                    + " in method '" + method.getName()
                    + "' does not match HTTP method "
                    + h.getOp() + " in link header");
        }

        // Create client request builder
        ClientRequest.Builder rb = ClientRequest.create();

        // Map method params to HTTP request
        mapParametersToClientRequest(rb, method, params);

        // Create URI builder and add query params
        UriBuilder ub = UriBuilder.fromUri(h.getUri());
        for (Map.Entry<String, List<String>> e : queryParams.entrySet()) {
            for (String v : e.getValue()) {
                ub.queryParam(e.getKey(), v);
            }
        }

        // Client side: produces is Accept
        Produces produces = method.getAnnotation(Produces.class);
        if (produces != null) {
            rb.accept(produces.value());
        }

        // Is there an entity?
        if (entity != null) {
            // Client side: consumes is Content-Type
            String contentType = null;
            Consumes consumes = method.getAnnotation(Consumes.class);
            if (consumes != null) {
                String[] contentTypes = consumes.value();
                if (contentTypes.length > 1) {
                    throw new RuntimeException("Annotation @Consumes in" +
                            " action '" + action.value() +
                            " ' must have a single media type");
                }
                contentType = contentTypes[0];
            } else if (entity instanceof MultivaluedMap) {
                // Guess content type if this is a Form
                contentType = MediaType.APPLICATION_FORM_URLENCODED;
            } else {
                contentType = getContentTypeFromWadl(action, method);
                assert contentType != null;
            }

            // Send HTTP request with entity
            ClientRequest cr = rb.entity(entity, contentType)
                                 .build(ub.build(), httpMethod);
            response = client.handle(cr);
        }
        else {
            // Send HTTP request without an entity
            ClientRequest cr = rb.build(ub.build(), httpMethod);
            response = client.handle(cr);
        }

        // Update action headers based on last response
        updateActionHeaders();

        // Update local model if returned
        HypermediaController hc = ctrlClass.getAnnotation(HypermediaController.class);
        assert hc != null;
        if (method.getReturnType() == hc.model()) {
            model = (T) response.getEntity(modelClass);
            response.close();
            return model;
        } else if (method.getReturnType() != void.class) {
            Object tmp = response.getEntity(method.getReturnType());
            response.close();
            return tmp;
        } else {
            return null;
        }
    }

    /**
     * Parses action headers and updates internal map.
     */
    private void updateActionHeaders() {
        actionHeaders.clear();
        List<String> actionHeadersList = response.getHeaders().get("Link");
        if (actionHeadersList != null) {
            for (String ah : actionHeadersList) {
                LinkHeader linkHeader = new LinkHeader(ah);
                actionHeaders.put(linkHeader.getRel(), linkHeader);
            }
        }
    }

    /**
     * Finds an annotation annotated with @HttpMethod in the
     * controller class (such as @GET, @PUT, etc.) or any of
     * its superclasses.
     */
    private String getHttpMethod(Class<?> ctrlClass, Method method) {
        // Find annotation in this class
        for (Annotation annot : method.getDeclaredAnnotations()) {
            HttpMethod httpMethod =
                    annot.annotationType().getAnnotation(HttpMethod.class);
            if (httpMethod != null) {
                return httpMethod.value();
            }
        }

        // Try super interfaces then
        Class<?>[] superInterfaces = ctrlClass.getInterfaces();
        for (Class<?> superInterface : superInterfaces) {
            for (Method superMethod : superInterface.getDeclaredMethods()) {
                if (superMethod.getName().equals(method.getName())
                        && superMethod.getReturnType() == method.getReturnType()
                        && Arrays.equals(superMethod.getParameterTypes(),
                                         method.getParameterTypes())) {
                    return getHttpMethod(superInterface, superMethod);
                }
            }
        }

        // Not found
        return null;
    }

    /**
     * Determines if an object is of any of the collection
     * types supported by JAX-RS for parameters.
     */
    private boolean isJaxrsCollectionValue(Object object) {
        return (object instanceof List || object instanceof Set
                || object instanceof SortedSet);
    }

    /**
     * Maps a method parameter in a controller interface to an
     * HTTP parameter or the entity. If not enough information
     * is available statically, use WADL to determine mapping.
     */
    private void mapParametersToClientRequest(ClientRequest.Builder rb,
            Method method, Object[] values)
    {
        // Inspect parameter annotations in method
        if (values != null) {
            Annotation[][] annotations = method.getParameterAnnotations();

            // Search for entity looking for params without annotations
            int j = 0;
            for (Object value: values) {
                if (annotations[j++].length == 0) {
                    entity = value;
                    break;
                }
            }

            // Collect all request values
            j = 0;
            for (Object value : values) {
                // If more than one annotation, report error
                if (annotations[j].length > 1) {
                    throw new IllegalArgumentException("At most one annotation"
                            + " is permitted in method '" + method.getName()
                            + "' of interface '" + ctrlClass.getName() + "'");
                }

                // Determine mapping for this value
                for (Annotation annot : annotations[j]) {
                    Class paramType = annot.annotationType();

                    // If @Name, determine param type looking at WADL
                    if (paramType == Name.class) {
                        mapParamUsingWadl(method, (Name) annot, value, rb);
                    }
                    else if (paramType == HeaderParam.class) {
                        final HeaderParam hp = (HeaderParam) annot;
                        setHeaderParam(hp.value(), value, rb);
                    }
                    else if (paramType == QueryParam.class) {
                        final QueryParam qp = (QueryParam) annot;
                        setQueryParam(qp.value(), value);
                    }
                    else if (paramType == CookieParam.class) {
                        setCookieParam(value, rb);
                    }
                    else if (paramType == FormParam.class) {
                        MultivaluedMap form = null;
                        final FormParam fp = (FormParam) annot;

                        // If entity found, check its type
                        if (entity != null) {
                            if (entity instanceof Form || entity instanceof MultivaluedMap) {
                                form = (MultivaluedMap) entity;
                            } else {
                                throw new IllegalArgumentException(
                                    "Unannotated parameter in method '"
                                    + method.getName()
                                    + "' in interface '" + ctrlClass.getName()
                                    + "' must be an instance of Form or "
                                    + " MultivaluedMap to support @FormParam");
                            }
                        } else {
                            entity = form = new Form();     // create entity
                        }

                        setFormParam(fp.value(), value, form);
                    }
                    else {
                        throw new IllegalArgumentException("Annotation "
                                + paramType.getName()
                                + " is not permitted in interface '"
                                + ctrlClass.getName() + "'");
                    }
                }

                j++;
            }
        }
    }

    /**
     * Maps a named method parameter in a controller interface
     * to an HTTP parameter or the entity using WADL.
     */
    private void mapParamUsingWadl(Method method, Name name, Object value,
            ClientRequest.Builder rb)
    {
        Action action = method.getAnnotation(Action.class);
        LinkHeader h = actionHeaders.get(action.value());
        MethodNode methodNode = getWadlMetadata(h);
        if (methodNode == null) {
            throw new RuntimeException("Unable to find WADL meta-data to " +
                    "map hypermedia action '" + action.value() + "'");
        }

        // Determine if this is a query param
        List<Param> params = methodNode.getQueryParameters();
        if (params != null) {
            for (Param param : params) {
                final String paramName = param.getName();
                if (paramName.equals(name.value())) {
                    setQueryParam(paramName, value);
                    return;
                }
            }
        }

        // Determine if this is a header or cookie param
        // Cookie params have name="Cookie" path="<name>"
        params = methodNode.getHeaderParameters();
        if (params != null) {
            for (Param param : params) {
                final String paramName = param.getName();
                if (paramName.equals(name.value())) {
                    setHeaderParam(paramName, value, rb);
                    return;
                } else if (paramName.equals("Cookie") &&
                          param.getPath().equals(name.value())) {
                    setCookieParam(value, rb);
                    return;
                }
            }
        }

        // Determine if this is a form param
        List<RepresentationNode> reps = methodNode.getSupportedInputs();
        if (reps != null) {
            for (RepresentationNode rep : reps) {
                // Media type must be application/x-www-form-urlencoded
                if (!rep.getMediaType().equals(APPLICATION_FORM_URLENCODED)) {
                    continue;
                }

                // Find param in representation
                for (Param param : rep.getParam()) {
                    if (param.getName().equals(name.value())) {
                        MultivaluedMap form = null;

                        // If entity found, check its type
                        if (entity != null) {
                            if (entity instanceof Form ||
                                    entity instanceof MultivaluedMap) {
                                form = (MultivaluedMap) entity;
                            } else {
                                throw new IllegalArgumentException(
                                    "Unannotated parameter in method '"
                                    + method.getName()
                                    + "' in interface '" + ctrlClass.getName()
                                    + "' must be an instance of Form or "
                                    + " MultivaluedMap to support @FormParam");
                            }
                        } else {
                            entity = form = new Form();     // create entity
                        }

                        setFormParam(name.value(), value, form);
                        return;
                    }
                }
            }
        }

        // Couldn't map, throw exception
        throw new RuntimeException(
                "Unable to map parameter '" + name.value()
                + "' in method '" + method.getName()
                + "' of interface '" + ctrlClass.getName() + "'");
    }

    /**
     * Returns the expected content-type of an action from its
     * WADL description. It reports an error if multiple media
     * types are supported as input or if unable to determine
     * what inputs are supported.
     */
    private String getContentTypeFromWadl(Action action, Method method) {
        // Get WADL method node for this action
        LinkHeader h = actionHeaders.get(action.value());
        MethodNode methodNode = getWadlMetadata(h);
        if (methodNode == null) {
            throw new RuntimeException("Unable to find WADL meta-data to " +
                    "map hypermedia action '" + action.value() + "'");
        }

        // Get list of inputs and make sure it's unambiguous
        List<RepresentationNode> l = methodNode.getSupportedInputs();
        if (l == null || l.size() > 1) {
            throw new RuntimeException("Unable to determine content type"
                    + " for action '" + action.value()
                    + "' -- use @Consumes annotation");
        }
        return l.get(0).getMediaType();
    }

    /**
     * Returns a <code>MethodNode</code> that represents the meta-data
     * for the link in header <code>h</code>, or <code>null</code>
     * if no meta-data was found. Uses OPTIONS to retrieve a WADL
     * fragment for the corresponding action.
     */
    private MethodNode getWadlMetadata(LinkHeader h) {
        MethodNode result = null;

        // Meta-data in cache?
        if (wadlMetadataCache == null) {
            wadlMetadataCache = new HashMap<String, MethodNode>();
        } else {
            result = wadlMetadataCache.get(h.getUri());
        }

        // Attempt to fetch meta-data from server
        if (result == null) {
            WadlModeller wm = new WadlModeller();
            try {
                WebResource r = client.resource(h.getUri());
                InputStream is = r.options(InputStream.class);

                // Requires WadlFragmentGetFilter on server side
                List<ResourceNode> rs = wm.process(new URI(h.getUri()), is);
                ResourceNode rn = rs.get(0).getChildResources().get(0);
                
                // Find method whose operation matches the link header's
                for (MethodNode mn : rn.getMethods()) {
                    if (mn.getName().equals(h.getOp())) {
                        result = mn;
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            wadlMetadataCache.put(h.getUri(), result);
        }
        return result;
    }

    /**
     * Sets a query param in request based on the type of value.
     */
    private void setQueryParam(String name, Object value) {
        if (isJaxrsCollectionValue(value)) {
            for (Object o : (Collection) value) {
                queryParams.add(name, o.toString());
            }
        } else {
            queryParams.add(name, value.toString());
        }
    }

    /**
     * Sets a header param in request based on the type of value.
     */
    private void setHeaderParam(String name, Object value,
            ClientRequest.Builder rb) {
        if (isJaxrsCollectionValue(value)) {
            for (Object o : (Collection) value) {
                rb.header(name, o);
            }
        } else {
            rb.header(name, value);
        }
    }

    /**
     * Sets a cookie param in request based on the type of value.
     */
    private void setCookieParam(Object value, ClientRequest.Builder rb) {
        if (isJaxrsCollectionValue(value)) {
            for (Object o : (Collection) value) {
                rb.cookie(o instanceof Cookie ? (Cookie) o
                        : Cookie.valueOf(o.toString()));
            }
        } else {
                rb.cookie(value instanceof Cookie ? (Cookie) value
                        : Cookie.valueOf(value.toString()));
        }
    }

    /**
     * Sets a form param in request based on the type of value.
     */
    private void setFormParam(String name, Object value, MultivaluedMap form) {
        if (isJaxrsCollectionValue(value)) {
            for (Object o : (Collection) value) {
                form.add(name, o);
            }
        } else {
            form.add(name, value);
        }
    }

}
