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

package com.sun.jersey.oauth.server;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.util.LazyVal;
import com.sun.jersey.oauth.server.api.OAuthServerFilter;
import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.spi.inject.ConstrainedTo;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.ServerSide;
import java.lang.reflect.Type;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/** Provider that handles the injection of the application-specific {@link OAuthProvider}
 * implementation.
 *
 * @author Martin Matula
 */
@Provider
@ConstrainedTo(ServerSide.class)
public class OAuthProviderInjectionProvider implements Injectable<OAuthProvider>, InjectableProvider<Context, Type> {
    private @Context ResourceConfig rc;

    private final LazyVal<OAuthProvider> instance = new LazyVal<OAuthProvider>() {
        @Override
        protected OAuthProvider instance() {
            // first find out what class is registered
            String providerClassName = (String) rc.getProperty(OAuthServerFilter.PROPERTY_PROVIDER);
            if (providerClassName == null) {
                throw new RuntimeException("Missing OAuthProvider class name in the configuration. Make sure '" + OAuthServerFilter.PROPERTY_PROVIDER + "' property is set.");
            }
            Class providerClass;
            try {
                providerClass = Class.forName(providerClassName, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Could not load OAuthProvider implementation: " + ex.getMessage(), ex);
            }

            OAuthProvider provider = null;
            for (Object obj : rc.getProviderSingletons()) {
                if (obj.getClass() == providerClass) {
                    provider = (OAuthProvider) obj;
                    break;
                }
            }
            if (provider == null) {
                try {
                    provider = (OAuthProvider) providerClass.newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException("Could not instantiate OAuthProvider class", ex);
                }
            }
            return provider;
        }
    };

    @Override
    public OAuthProvider getValue() {
        return instance.get();
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }

    @Override
    public Injectable getInjectable(ComponentContext cc, Context a, Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            if (c.isInstance(instance.get()) && OAuthProvider.class.isAssignableFrom(c)) {
                return this;
            }
        }

        return null;
    }
}
