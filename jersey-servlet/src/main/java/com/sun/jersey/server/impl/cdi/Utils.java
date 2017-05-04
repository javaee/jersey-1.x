/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
  * Utility methods for CDI BeanManager and Bean classes.
  *
  * @author Roberto Chinnici
  * @author Paul Sandoz (paul.sandoz at oracle.com)
  * @author Jakub Podlesak (jakub.podlesak at oracle.com)
  */
public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    /**
     * To prevent instantiation.
     */
    private Utils() {}

    /**
     * Gets you a CDI bean that corresponds to the type provided as parameter.
     *
     * @param beanManager bean manager to get the bean from
     * @param clazz type for which a corresponding bean should be found
     * @return CDI bean for given type, or null if no such bean have been found
     *
     */
    public static Bean<?> getBean(BeanManager beanManager, Class<?> clazz) {

        final Set<Bean<?>> beans = beanManager.getBeans(clazz);
        if (beans.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("No CDI beans found in bean manager, %s, for type %s", beanManager, clazz));
            }
            return null;
        }

        try {
            return beanManager.resolve(beans);
        } catch(AmbiguousResolutionException ex) {
            // Check if there is a shared base class of c
            // If so reduce the set of beans whose class equals c and resolve
            if (isSharedBaseClass(clazz, beans)) {
                try {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Ambiguous resolution exception caught when resolving bean %s. Trying to resolve by the type %s", beans, clazz));
                    }
                    return beanManager.resolve(getBaseClassSubSet(clazz, beans));
                } catch (AmbiguousResolutionException ex2) {
                    return null;
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Failed to resolve bean %s.", beans));
            }
            return null;
        }
    }

    public static <T> T getInstance(BeanManager beanManager, Class<T> c) {

        Bean<?> bean = getBean(beanManager, c);
        if (bean == null) {
            return null;
        }

        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
        final Object result = beanManager.getReference(bean, c, creationalContext);

        return c.cast(result);
    }

    public static CDIExtension getCdiExtensionInstance(BeanManager beanManager) {
        Bean<?> bean = getBean(beanManager, CDIExtension.class);
        if  (bean == null) {
            return null;
        }

        return ( CDIExtension ) beanManager.getContext( ApplicationScoped.class ).get( bean );
    }

    private static boolean isSharedBaseClass(final Class<?> clazz, final Set<Bean<?>> beans) {
        for (Bean<?> bean : beans) {
            if (!clazz.isAssignableFrom(bean.getBeanClass())) {
                return false;
            }
        }
        return true;
    }

    private static Set<Bean<?>> getBaseClassSubSet(final Class<?> clazz, final Set<Bean<?>> beans) {
        for (Bean<?> bean : beans) {
            if (clazz == bean.getBeanClass()) {
                return Collections.<Bean<?>>singleton(bean);
            }
        }
        return beans;
    }
}
