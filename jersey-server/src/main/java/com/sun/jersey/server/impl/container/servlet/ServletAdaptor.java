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

package com.sun.jersey.server.impl.container.servlet;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletConfig;

/**
 * A servlet container for deploying root resource classes with support
 * for injecting persistence units.
 * <p>
 * Persistence units that may be injected must be configured in web.xml
 * in the normal way plus an additional servlet parameter to enable the
 * Jersey servlet to locate them in JNDI. E.g. with the following
 * persistence unit configuration:
 *
 * <persistence-unit-ref>
 *     <persistence-unit-ref-name>persistence/widget</persistence-unit-ref-name>
 *     <persistence-unit-name>WidgetPU</persistence-unit-name>
 * </persistence-unit-ref>
 *
 * the Jersey servlet requires an additional servlet parameter as
 * follows:
 *
 * <init-param>
 *     <param-name>unit:WidgetPU</param-name>
 *     <param-value>persistence/widget</param-value>
 * </init-param>
 *
 * Given the above, Jersey will inject the EntityManagerFactory found
 * at java:comp/env/persistence/widget in JNDI when encountering a
 * field or parameter annotated with @PersistenceUnit(unitName="WidgetPU").
 */
public class ServletAdaptor extends ServletContainer {
    private Map<String, String> persistenceUnits =
            new HashMap<String, String>();
        
    @Override
    protected void configure(ServletConfig servletConfig, ResourceConfig rc, WebApplication wa) {
        super.configure(servletConfig, rc, wa);

        /**
         * Look for persistent units.
         */
        for (Enumeration e = servletConfig.getInitParameterNames() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String value = servletConfig.getInitParameter(key);
            if (key.startsWith("unit:")) {
                persistenceUnits.put(key.substring(5),"java:comp/env/"+value);
            }
        }
        
        rc.getSingletons().add(new InjectableProvider<PersistenceUnit, Type>() {
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }
            
            public Injectable<EntityManagerFactory> getInjectable(ComponentContext ic, PersistenceUnit pu, Type c) {
                if (!c.equals(EntityManagerFactory.class))
                    return null;
                
                // TODO localize error message
                if (!persistenceUnits.containsKey(pu.unitName()))
                    throw new ContainerException("Persistence unit '"+
                            pu.unitName()+
                            "' is not configured as a servlet parameter in web.xml");
                String jndiName = persistenceUnits.get(pu.unitName());
                ThreadLocalNamedInvoker<EntityManagerFactory> emfHandler =
                        new ThreadLocalNamedInvoker<EntityManagerFactory>(jndiName);
                final EntityManagerFactory emf = (EntityManagerFactory) Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[] { EntityManagerFactory.class },
                        emfHandler);
                
                return new Injectable<EntityManagerFactory>() {
                    public EntityManagerFactory getValue() {
                        return emf;
                    }                    
                };
            }
        });        
    }    
}
