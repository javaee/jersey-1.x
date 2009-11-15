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
package com.sun.jersey.server.impl.ejb;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class EJBComponentProviderFactory implements
        IoCComponentProviderFactory,
        IoCComponentProcessorFactoryInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(
            EJBComponentProviderFactory.class.getName());

    private final EJBInjectionInterceptor interceptor;

    public EJBComponentProviderFactory(EJBInjectionInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    // IoCComponentProviderFactory
    
    public IoCComponentProvider getComponentProvider(Class<?> c) {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, Class<?> c) {
        String name = getName(c);
        if (name == null) {
            return null;
        }

        try {
            InitialContext ic = new InitialContext();
            Object o = lookup(ic, c, name);

            LOGGER.info("Binding the EJB class " + c.getName() +
                    " to EJBManagedComponentProvider");

            return new EJBManagedComponentProvider(o);
        } catch (NamingException ex) {
            String message =  "An instance of EJB class " + c.getName() +
                    " could not be looked up using simple form name or the fully-qualified form name." +
                    "Ensure that the EJB/JAX-RS component implements at most one interface.";
            LOGGER.log(Level.SEVERE, message, ex);
            throw new ContainerException(message);
        }
    }

    private String getName(Class<?> c) {
        String name = null;
        if (c.isAnnotationPresent(Stateless.class)) {
            name = c.getAnnotation(Stateless.class).name();
        } else if (c.isAnnotationPresent(Singleton.class)) {
            name = c.getAnnotation(Singleton.class).name();
        } else {
            return null;
        }

        if (name == null || name.length() == 0) {
            name = c.getSimpleName();
        }
        return name;
    }

    private Object lookup(InitialContext ic, Class<?> c, String name) throws NamingException {
        try {
            return lookupSimpleForm(ic, c, name);
        } catch (NamingException ex) {
            LOGGER.log(Level.WARNING, "An instance of EJB class " + c.getName() +
                    " could not be looked up using simple form name. " +
                    "Attempting to look up using the fully-qualified form name.", ex);

            return lookupFullyQualfiedForm(ic, c, name);
        }
    }

    private Object lookupSimpleForm(InitialContext ic, Class<?> c, String name) throws NamingException {
        String jndiName = "java:module/" + name;
        return ic.lookup(jndiName);
    }

    private Object lookupFullyQualfiedForm(InitialContext ic, Class<?> c, String name) throws NamingException {
        String jndiName =  "java:module/" + name + "!" + c.getName();
        return ic.lookup(jndiName);
    }


    private static class EJBManagedComponentProvider implements IoCFullyManagedComponentProvider {
        private final Object o;

        EJBManagedComponentProvider(Object o) {
            this.o = o;
        }

        public ComponentScope getScope() {
            return ComponentScope.Singleton;
        }

        public Object getInstance() {
            return o;
        }
    }
    
    // IoCComponentProcessorFactoryInitializer
    
    public void init(IoCComponentProcessorFactory cpf) {
        interceptor.setFactory(cpf);
    }
}