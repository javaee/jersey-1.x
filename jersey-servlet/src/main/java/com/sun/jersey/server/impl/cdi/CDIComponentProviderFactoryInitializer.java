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
package com.sun.jersey.server.impl.cdi;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.InitialContextHelper;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.WebConfig;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the factory for IoCComponentProvider(s) for CDI beans.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author robc
 */
public class CDIComponentProviderFactoryInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(CDIComponentProviderFactoryInitializer.class.getName());

    private static final String BEAN_MANAGER_CLASS = "javax.enterprise.inject.spi.BeanManager";
    private static final String WELD_SERVLET_PACKAGE = "org.jboss.weld.environment.servlet";

    public static void initialize(WebConfig wc, ResourceConfig rc, WebApplication wa) {
        ServletContext sc = wc.getServletContext();

        Object beanManager = lookup(sc);
        if (beanManager == null) {
            LOGGER.config("The CDI BeanManager is not available. JAX-RS CDI support is disabled.");
            return;
        }

        rc.getSingletons().add(new CDIComponentProviderFactory(beanManager, rc, wa));
        LOGGER.info("CDI support is enabled");
    }

    private static Object lookup(ServletContext sc) {
        Object beanManager = null;

        beanManager = lookupInJndi("java:comp/BeanManager");
        if (beanManager != null) {
            return beanManager;
        }

        // Standard in CDI 1.1
        beanManager = lookupInServletContext(sc, BEAN_MANAGER_CLASS);
        if (beanManager != null) {
            return beanManager;
        }

        // For older Weld versions
        beanManager = lookupInServletContext(sc, WELD_SERVLET_PACKAGE + "." + BEAN_MANAGER_CLASS);
        if (beanManager != null) {
            return beanManager;
        }

        return null;
    }

    private static Object lookupInJndi(String name) {
        try {
            InitialContext ic = InitialContextHelper.getInitialContext();
            if (ic == null) {
                return null;
            }

            Object beanManager = ic.lookup(name);
            // Some implementations of InitialContext return null instead of
            // throwing NamingException if there is no Object associated with
            // the name
            if (beanManager == null) {
                LOGGER.config("The CDI BeanManager is not available at " + name);
                return null;
            }

            LOGGER.config("The CDI BeanManager is at " + name);
            return beanManager;
            
        } catch (NamingException ex) {
            LOGGER.log(Level.CONFIG, "The CDI BeanManager is not available at " + name, ex);
        }

        return null;
    }

    private static Object lookupInServletContext(ServletContext sc, String name) {
        Object beanManager = sc.getAttribute(name);
        if (beanManager == null) {
            LOGGER.config("The CDI BeanManager is not available at " + name);
            return null;
        }

        LOGGER.config("The CDI BeanManager is at " + name);
        return beanManager;
    }
}
