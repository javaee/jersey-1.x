/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.inject;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.AnnotatedContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.factory.InjectableProviderFactory;
import com.sun.jersey.spi.inject.Injectable;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ServerInjectableProviderFactory extends InjectableProviderFactory
            implements ServerInjectableProviderContext {

    @Override
    public boolean isParameterTypeRegistered(Parameter p) {
        if (p.getAnnotation() == null) return false;

        if (isAnnotationRegistered(p.getAnnotation().annotationType(), p.getClass())) return true;

        return isAnnotationRegistered(p.getAnnotation().annotationType(), p.getParameterType().getClass());
    }

    @Override
    public InjectableScopePair getInjectableiWithScope(Parameter p, ComponentScope s) {
        return getInjectableiWithScope(null, p, s);
    }

    @Override
    public InjectableScopePair getInjectableiWithScope(AccessibleObject ao, Parameter p, ComponentScope s) {
        if (p.getAnnotation() == null) return null;

        ComponentContext ic = new AnnotatedContext(ao, p.getAnnotations());

        if (s == ComponentScope.PerRequest) {
            // Find a per request injectable with Parameter
            Injectable i = getInjectable(
                    p.getAnnotation().annotationType(),
                    ic,
                    p.getAnnotation(),
                    p,
                    ComponentScope.PerRequest);
            if (i != null) return new InjectableScopePair(i, ComponentScope.PerRequest);

            // Find a per request, undefined or singleton injectable with parameter Type
            return getInjectableWithScope(
                    p.getAnnotation().annotationType(),
                    ic,
                    p.getAnnotation(),
                    p.getParameterType(),
                    ComponentScope.PERREQUEST_UNDEFINED_SINGLETON
                    );
        } else {
            // Find a undefined or singleton injectable with parameter Type
            return getInjectableWithScope(
                    p.getAnnotation().annotationType(),
                    ic,
                    p.getAnnotation(),
                    p.getParameterType(),
                    ComponentScope.UNDEFINED_SINGLETON
                    );
        }
    }

    @Override
    public Injectable getInjectable(Parameter p, ComponentScope s) {
        return getInjectable(null, p, s);
    }

    @Override
    public Injectable getInjectable(AccessibleObject ao, Parameter p, ComponentScope s) {
        InjectableScopePair isp = getInjectableiWithScope(ao, p, s);
        if (isp == null)
            return null;
        return isp.i;
    }

    @Override
    public List<Injectable> getInjectable(List<Parameter> ps, ComponentScope s) {
        return getInjectable(null, ps, s);
    }

    @Override
    public List<Injectable> getInjectable(AccessibleObject ao, List<Parameter> ps, ComponentScope s) {
        List<Injectable> is = new ArrayList<Injectable>();
        for (Parameter p : ps)
            is.add(getInjectable(ao, p, s));
        return is;
    }
}