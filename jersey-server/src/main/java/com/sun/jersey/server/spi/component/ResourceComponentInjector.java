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
package com.sun.jersey.server.spi.component;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractField;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractSetterMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.spi.inject.InjectableProviderContext.InjectableScopePair;
import com.sun.jersey.spi.inject.Errors;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An injector that injects onto properties of a resource.
 * 
 * Analysis of the class will be performed using reflection to find 
 * {@link Injectable} instances and as a result the use of reflection is 
 * minimized when performing injection.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceComponentInjector {
    private Field[] singletonFields;    
    private Object[] singletonFieldValues;
    
    private Field[] perRequestFields;
    private AbstractHttpContextInjectable<?>[] perRequestFieldInjectables;
    
    private Method[] singletonSetters;    
    private Object[] singletonSetterValues;
    
    private Method[] perRequestSetters;
    private AbstractHttpContextInjectable<?>[] perRequestSetterInjectables;
    
    /**
     * Create a new resource component injector.
     * 
     * @param ipc the injectable provider context to obtain injectables.
     * @param s the scope under which injection will be performed.
     * @param resource the abstract resource model.
     */
    public ResourceComponentInjector(ServerInjectableProviderContext ipc, ComponentScope s, AbstractResource resource) {
        // processFields(ipc, s, resource.getResourceClass());
        processFields(ipc, s, resource.getFields());
        processSetters(ipc, s, resource.getSetterMethods());
    }

    /**
     * Ascertain if there are any injectable artifacts to be injected.
     *
     * @return true if there are any injectable artifacts to be injected.
     */
    public boolean hasInjectableArtifacts() {
        return singletonFields.length > 0 || perRequestFields.length > 0 ||
                singletonSetters.length > 0 || perRequestSetters.length > 0;
    }

    private void processFields(ServerInjectableProviderContext ipc, ComponentScope s,
            List<AbstractField> fields) {
        Map<Field, Injectable<?>> singletons = new HashMap<Field, Injectable<?>>();
        Map<Field, Injectable<?>> perRequest = new HashMap<Field, Injectable<?>>();
        
        for (AbstractField af : fields) {
            Parameter p = af.getParameters().get(0);

            InjectableScopePair isp = ipc.getInjectableiWithScope(af.getField(), p, s);
            if (isp != null) {
                configureField(af.getField());
                if (s == ComponentScope.PerRequest && isp.cs != ComponentScope.Singleton) {
                    perRequest.put(af.getField(), isp.i);
                } else {
                    singletons.put(af.getField(), isp.i);
                }
            } else if (ipc.isParameterTypeRegistered(p)) {
                // Missing dependency
                Errors.missingDependency(af.getField());
            }
        }
        
        int size = singletons.entrySet().size();
        singletonFields = new Field[size];
        singletonFieldValues = new Object[size];        
        int i = 0;
        for (Map.Entry<Field, Injectable<?>> e : singletons.entrySet()) {
            singletonFields[i] = e.getKey();
            singletonFieldValues[i++] = e.getValue().getValue();
        }
        
        size = perRequest.entrySet().size();
        perRequestFields = new Field[size];
        perRequestFieldInjectables = new AbstractHttpContextInjectable<?>[size];
        i = 0;
        for (Map.Entry<Field, Injectable<?>> e : perRequest.entrySet()) {
            perRequestFields[i] = e.getKey();
            perRequestFieldInjectables[i++] = AbstractHttpContextInjectable.transform(e.getValue());
        }        
    }
    
    private void configureField(final Field f) {
        if (!f.isAccessible()) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    f.setAccessible(true);
                    return null;
                }
            });
        }
    }
    
    private void processSetters(ServerInjectableProviderContext ipc, ComponentScope s,
            List<AbstractSetterMethod> setterMethods) {
        Map<Method, Injectable<?>> singletons = new HashMap<Method, Injectable<?>>();
        Map<Method, Injectable<?>> perRequest = new HashMap<Method, Injectable<?>>();
        
        int methodIndex = 0;
        for (AbstractSetterMethod sm : setterMethods) {
            Parameter p = sm.getParameters().get(0);

            InjectableScopePair isp = ipc.getInjectableiWithScope(sm.getMethod(), p, s);
            if (isp != null) {
                if (s == ComponentScope.PerRequest && isp.cs != ComponentScope.Singleton) {
                    perRequest.put(sm.getMethod(), isp.i);
                } else {
                    singletons.put(sm.getMethod(), isp.i);
                }
            } else if (ipc.isParameterTypeRegistered(p)) {
                // Missing dependency
                Errors.missingDependency(sm.getMethod(), methodIndex);
            }
            methodIndex++;
        }
                
        int size = singletons.entrySet().size();
        singletonSetters = new Method[size];
        singletonSetterValues = new Object[size];        
        int i = 0;
        for (Map.Entry<Method, Injectable<?>> e : singletons.entrySet()) {
            singletonSetters[i] = e.getKey();
            singletonSetterValues[i++] = e.getValue().getValue();
        }
        
        size = perRequest.entrySet().size();
        perRequestSetters = new Method[size];
        perRequestSetterInjectables = new AbstractHttpContextInjectable<?>[size];
        i = 0;
        for (Map.Entry<Method, Injectable<?>> e : perRequest.entrySet()) {
            perRequestSetters[i] = e.getKey();
            perRequestSetterInjectables[i++] = AbstractHttpContextInjectable.transform(e.getValue());
        }        
    }
    
    /**
     * Inject onto an instance of a resource class.
     * 
     * @param c the HTTP context, may be set to null if not available for the
     *        current scope.
     * @param o the resource.
     */
    public void inject(HttpContext c, Object o) {
        int i = 0;
        for (Field f : singletonFields) {
            try {
                f.set(o, singletonFieldValues[i++]);
            } catch (IllegalAccessException ex) {
                throw new ContainerException(ex);
            }
        }
        
        i = 0;
        for (Field f : perRequestFields) {
            try {
                f.set(o, perRequestFieldInjectables[i++].getValue(c));
            } catch (IllegalAccessException ex) {
                throw new ContainerException(ex);
            }
        }
        
        i = 0;
        for (Method m : singletonSetters) {
            try {
                m.invoke(o, singletonSetterValues[i++]);
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }
        }
        
        i = 0;
        for (Method m : perRequestSetters) {
            try {
                m.invoke(o, perRequestSetterInjectables[i++].getValue(c));
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }
        }
    }
}