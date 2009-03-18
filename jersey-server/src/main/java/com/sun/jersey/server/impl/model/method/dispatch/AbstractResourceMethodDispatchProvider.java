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

package com.sun.jersey.server.impl.model.method.dispatch;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.server.impl.ResponseBuilderImpl;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;


/**
 * A partial implementation of {@link ResourceMethodDispatchProvider} that
 * creates instances of {@link RequestDispatcher}.
 * <p>
 * Implementing classes are required to override the {@link #getInjectableValuesProvider(com.sun.jersey.api.model.AbstractResourceMethod) }
 * method to return a {@link InjectableValuesProvider} associated with the parameters
 * of the abstract resource method.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {

    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        
        InjectableValuesProvider pp = getInjectableValuesProvider(abstractResourceMethod);
        if (pp == null)
            return null;

        // TODO
        // Strictly speaking a GET request can contain an entity in the
        // message body, but this is likely to be not implemented by many
        // servers and clients, but should we support it?
        boolean requireReturnOfRepresentation =
                "GET".equals(abstractResourceMethod.getHttpMethod());

        Class<?> returnType = abstractResourceMethod.getMethod().getReturnType();
        if (Response.class.isAssignableFrom(returnType)) {
            return new ResponseOutInvoker(abstractResourceMethod, pp);
        } else if (returnType != void.class) {
            if (returnType == Object.class || GenericEntity.class.isAssignableFrom(returnType)) {
                return new ObjectOutInvoker(abstractResourceMethod, pp);
            } else {
                return new TypeOutInvoker(abstractResourceMethod, pp);
            }
        } else if (requireReturnOfRepresentation) {
            return null;
        } else {
            return new VoidOutInvoker(abstractResourceMethod, pp);
        }
    }

    private @Context ServerInjectableProviderContext sipc;

    /**
     * Get the server-specific injectable provider context.
     *
     * @return the server-specific injectable provider context
     */
    protected ServerInjectableProviderContext getInjectableProviderContext() {
        return sipc;
    }

    /**
     * Get the injectable values provider for an abstract resource method.
     * 
     * @param abstractResourceMethod the abstract resource method.
     * @return the injectable values provider, or null if no injectable values
     *         can be created for the parameters of the abstract
     *         resource method.
     */
    protected abstract InjectableValuesProvider getInjectableValuesProvider(AbstractResourceMethod abstractResourceMethod);


    private static abstract class EntityParamInInvoker extends ResourceJavaMethodDispatcher {
        private final InjectableValuesProvider pp;

        EntityParamInInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp) {
            super(abstractResourceMethod);
            this.pp = pp;
        }

        final Object[] getParams(HttpContext context) {
            return pp.getInjectableValues(context);
        }
    }

    private static final class VoidOutInvoker extends EntityParamInInvoker {
        VoidOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp) {
            super(abstractResourceMethod, pp);
        }

        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);
            method.invoke(resource, params);
        }
    }

    private static final class TypeOutInvoker extends EntityParamInInvoker {
        private final Type t;

        TypeOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp) {
            super(abstractResourceMethod, pp);
            this.t = abstractResourceMethod.getMethod().getGenericReturnType();
        }

        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            Object o = method.invoke(resource, params);
            if (o != null) {
                Response r = new ResponseBuilderImpl().
                        entityWithType(o, t).status(200).build();
                context.getResponse().setResponse(r);
            }
        }
    }

    private static final class ResponseOutInvoker extends EntityParamInInvoker {
        ResponseOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp) {
            super(abstractResourceMethod, pp);
        }

        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            Response r = (Response)method.invoke(resource, params);
            if (r != null) {
                context.getResponse().setResponse(r);
            }
        }
    }

    private static final class ObjectOutInvoker extends EntityParamInInvoker {
        ObjectOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp) {
            super(abstractResourceMethod, pp);
        }

        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            Object o = method.invoke(resource, params);

            if (o instanceof Response) {
                Response r = (Response)o;
                context.getResponse().setResponse(r);
            } else if (o != null) {
                Response r = new ResponseBuilderImpl().status(200).entity(o).build();
                context.getResponse().setResponse(r);
            }
        }
    }
}