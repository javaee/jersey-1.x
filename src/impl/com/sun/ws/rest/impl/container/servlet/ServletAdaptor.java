/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.container.servlet;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.container.servlet.ServletContainer;
import com.sun.ws.rest.spi.resource.Injectable;
import java.lang.reflect.Proxy;
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
        
        wa.addInjectable(EntityManagerFactory.class,
                new Injectable<PersistenceUnit, EntityManagerFactory>() {
            public Class<PersistenceUnit> getAnnotationClass() {
                return PersistenceUnit.class;
            }
            public EntityManagerFactory getInjectableValue(PersistenceUnit pu) {
                // TODO localize error message
                if (!persistenceUnits.containsKey(pu.unitName()))
                    throw new ContainerException("Persistence unit '"+
                            pu.unitName()+
                            "' is not configured as a servlet parameter in web.xml");
                String jndiName = persistenceUnits.get(pu.unitName());
                ThreadLocalNamedInvoker<EntityManagerFactory> emfHandler =
                        new ThreadLocalNamedInvoker<EntityManagerFactory>(jndiName);
                EntityManagerFactory emf = (EntityManagerFactory) Proxy.newProxyInstance(
                        EntityManagerFactory.class.getClassLoader(),
                        new Class[] {EntityManagerFactory.class },
                        emfHandler);
                return emf;
            }
        }
        );
    }    
}
