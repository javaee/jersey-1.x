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
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityParamDispatchProvider extends AbstractResourceMethodDispatchProvider {
                
    @Override
    protected InjectableValuesProvider getInjectableValuesProvider(AbstractResourceMethod abstractResourceMethod) {
        boolean requireNoEntityParameter =
                "GET".equals(abstractResourceMethod.getHttpMethod());

        List<Injectable> is = processParameters(abstractResourceMethod,
                requireNoEntityParameter);
        if (is == null)
            return null;

        return new InjectableValuesProvider(is);
    }

    private List<Injectable> processParameters(AbstractResourceMethod method,
            boolean requireNoEntityParameter) {
        
        if ((null == method.getParameters()) || (0 == method.getParameters().size())) {
            return Collections.emptyList();
        }

        boolean hasEntity = false;
        List<Injectable> is = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter parameter = method.getParameters().get(i);
            
            if (Parameter.Source.ENTITY == parameter.getSource()) {
                hasEntity = true;
                is.add(processEntityParameter(method,
                        parameter,
                        method.getMethod().getParameterAnnotations()[i],
                        requireNoEntityParameter));
            } else {
                is.add(getInjectableProviderContext().
                        getInjectable(parameter, ComponentScope.PerRequest));
            }
        }

        if (is.contains(null)) {
            int n = 0;
            for (Injectable i : is)
                if (i == null) n++;

            // If there is more than one null injectable or
            // if there is only one null injectable but there is an entity
            // injectable then return null
            if (n > 1 || hasEntity)
                return null;

            // Otherwise create the entity injectable
            for (int i = 0; i < is.size(); i++) {
                if (is.get(i) == null) {
                    Injectable ij = processEntityParameter(method,
                        method.getParameters().get(i),
                        method.getMethod().getParameterAnnotations()[i],
                        requireNoEntityParameter);
                    is.set(i, ij);
                    break;
                }
            }

            if (is.contains(null))
                return null;
        }
        return is;
    }

    static final class EntityInjectable extends AbstractHttpContextInjectable<Object> {
        final Class<?> c;
        final Type t;
        final Annotation[] as;

        EntityInjectable(Class c, Type t, Annotation[] as) {
            this.c = c;
            this.t = t;
            this.as = as;
        }

        public Object getValue(HttpContext context) {
            return context.getRequest().getEntity(c, t, as);
        }
    }
        
    private Injectable processEntityParameter(
            AbstractResourceMethod method,
            Parameter parameter,
            Annotation[] annotations,
            boolean requireNoEntityParameter) {
        if (requireNoEntityParameter) {
            // Entity as a method parameterClass is not required
            return null;
        }

        if (parameter.getParameterType() instanceof TypeVariable) {
            ReflectionHelper.ClassTypePair ct = ReflectionHelper.resolveTypeVariable(
                    method.getDeclaringResource().getResourceClass(),
                    method.getMethod().getDeclaringClass(),
                    (TypeVariable)parameter.getParameterType());

            return (ct != null) ? new EntityInjectable(ct.c, ct.t, annotations) : null;
        } else {
            return new EntityInjectable(parameter.getParameterClass(),
                    parameter.getParameterType(), annotations);
        }
    }
}