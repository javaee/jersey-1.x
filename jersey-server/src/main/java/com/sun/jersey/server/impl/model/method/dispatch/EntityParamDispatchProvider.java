/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.spi.container.ParamQualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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
        return new InjectableValuesProvider(processParameters(abstractResourceMethod));
    }

    private List<Injectable> processParameters(AbstractResourceMethod method) {
        
        if ((null == method.getParameters()) || (0 == method.getParameters().size())) {
            return Collections.emptyList();
        }

        boolean hasEntity = false;
        final List<Injectable> is = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            final Parameter parameter = method.getParameters().get(i);
            
            if (Parameter.Source.ENTITY == parameter.getSource()) {
                hasEntity = true;
                is.add(processEntityParameter(
                        parameter,
                        method.getMethod().getParameterAnnotations()[i]));
            } else {
                is.add(getInjectableProviderContext().
                        getInjectable(method.getMethod(), parameter, ComponentScope.PerRequest));
            }
        }

        if (hasEntity)
            return is;

        // Try to find entity if there is one unresolved parameter and
        // the annotations are unknown
        if (Collections.frequency(is, null) == 1) {
            final int i = is.lastIndexOf(null);
            final Parameter parameter = method.getParameters().get(i);
            if (Parameter.Source.UNKNOWN == parameter.getSource()) {
                if (!parameter.isQualified()) {
                    final Injectable ij = processEntityParameter(
                        parameter,
                        method.getMethod().getParameterAnnotations()[i]);
                    is.set(i, ij);
                }
            }
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
            Parameter parameter,
            Annotation[] annotations) {
        return new EntityInjectable(parameter.getParameterClass(),
                parameter.getParameterType(), annotations);
    }
}