/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.server.impl.model.method.dispatch;

import com.sun.jersey.spi.container.JavaMethodInvokerFactory;
import com.sun.jersey.spi.container.JavaMethodInvoker;
import com.sun.jersey.spi.container.ResourceMethodCustomInvokerDispatchProvider;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.api.JResponse;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Errors;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;


/**
 * A partial implementation of {@link ResourceMethodDispatchProvider} that
 * creates instances of {@link RequestDispatcher}.
 * <p>
 * Implementing classes are required to override the
 * {@link #getInjectableValuesProvider(com.sun.jersey.api.model.AbstractResourceMethod) }
 * method to return a {@link InjectableValuesProvider} associated with the parameters
 * of the abstract resource method.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractResourceMethodDispatchProvider implements ResourceMethodDispatchProvider, ResourceMethodCustomInvokerDispatchProvider {

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        return this.create(abstractResourceMethod, JavaMethodInvokerFactory.getDefault());
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod, JavaMethodInvoker invoker) {
        
        final InjectableValuesProvider pp = getInjectableValuesProvider(abstractResourceMethod);
        if (pp == null) {
            return null;
        }
        
        if (pp.getInjectables().contains(null)) {
            // Missing dependency
            for (int i = 0; i < pp.getInjectables().size(); i++) {
                if (pp.getInjectables().get(i) == null) {
                    Errors.missingDependency(abstractResourceMethod.getMethod(), i);
                }
            }
            return null;
        }

        final Class<?> returnType = abstractResourceMethod.getReturnType();
        if (Response.class.isAssignableFrom(returnType)) {
            return new ResponseOutInvoker(abstractResourceMethod, pp, invoker);
        } else if (JResponse.class.isAssignableFrom(returnType)) {
            return new JResponseOutInvoker(abstractResourceMethod, pp, invoker);
        } else if (returnType != void.class) {
            if (returnType == Object.class || GenericEntity.class.isAssignableFrom(returnType)) {
                return new ObjectOutInvoker(abstractResourceMethod, pp, invoker);
            } else {
                return new TypeOutInvoker(abstractResourceMethod, pp, invoker);
            }
        } else {
            return new VoidOutInvoker(abstractResourceMethod, pp, invoker);
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
    protected abstract InjectableValuesProvider getInjectableValuesProvider(
            AbstractResourceMethod abstractResourceMethod);


    private static abstract class EntityParamInInvoker extends ResourceJavaMethodDispatcher {
        private final InjectableValuesProvider pp;

        EntityParamInInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp) {
            this(abstractResourceMethod, pp, JavaMethodInvokerFactory.getDefault());
        }

        EntityParamInInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, invoker);
            this.pp = pp;
        }

        final Object[] getParams(HttpContext context) {
            return pp.getInjectableValues(context);
        }
    }

    private static final class VoidOutInvoker extends EntityParamInInvoker {
        VoidOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, pp, invoker);
        }

        @Override
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);
            invoker.invoke(method, resource, params);
        }
    }

    private static final class TypeOutInvoker extends EntityParamInInvoker {
        private final Type t;

        TypeOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, pp, invoker);
            this.t = abstractResourceMethod.getGenericReturnType();
        }

        @Override
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            final Object o = invoker.invoke(method, resource, params);
            if (o != null) {
                Response r = new ResponseBuilderImpl().
                        entityWithType(o, t).status(200).build();
                context.getResponse().setResponse(r);
            }
        }
    }

    private static final class ResponseOutInvoker extends EntityParamInInvoker {
        ResponseOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, pp, invoker);
        }

        @Override
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            final Response r = (Response)invoker.invoke(method, resource, params);
            if (r != null) {
                context.getResponse().setResponse(r);
            }
        }
    }

    private static final class JResponseOutInvoker extends EntityParamInInvoker {
        private final Type t;

        JResponseOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, pp);
            final Type jResponseType = abstractResourceMethod.getGenericReturnType();
            if (jResponseType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)jResponseType;
                if (pt.getRawType().equals(JResponse.class)) {
                    t = ((ParameterizedType)jResponseType).getActualTypeArguments()[0];
                } else {
                    t = null;
                }
            } else {
                t = null;
            }
        }

        @Override
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            final JResponse<?> r = (JResponse<?>)invoker.invoke(method, resource, params);
            if (r != null) {
                if (t == null) {
                    context.getResponse().setResponse(r.toResponse());
                } else {
                    context.getResponse().setResponse(r.toResponse(t));
                }
            }
        }
    }

    private static final class ObjectOutInvoker extends EntityParamInInvoker {
        ObjectOutInvoker(AbstractResourceMethod abstractResourceMethod,
                InjectableValuesProvider pp, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, pp, invoker);
        }

        @Override
        public void _dispatch(Object resource, HttpContext context)
        throws IllegalAccessException, InvocationTargetException {
            final Object[] params = getParams(context);

            final Object o = invoker.invoke(method, resource, params);

            if (o instanceof Response) {
                context.getResponse().setResponse((Response)o);
            } else if (o instanceof JResponse) {
                context.getResponse().setResponse(((JResponse)o).toResponse());
            } else if (o != null) {
                final Response r = new ResponseBuilderImpl().status(200).entity(o).build();
                context.getResponse().setResponse(r);
            }
        }
    }
}