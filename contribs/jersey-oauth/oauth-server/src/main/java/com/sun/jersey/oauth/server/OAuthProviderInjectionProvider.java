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

package com.sun.jersey.oauth.server;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.spi.inject.ConstrainedTo;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.ServerSide;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;

/** Provider that handles the injection of the application-specific {@link OAuthProvider}
 * implementation.
 *
 * @author Martin Matula
 */
@ConstrainedTo(ServerSide.class)
public class OAuthProviderInjectionProvider implements Injectable<OAuthProvider>, InjectableProvider<Context, Type> {
    private static final Logger LOGGER = Logger.getLogger(OAuthProviderInjectionProvider.class.getName());

    private final OAuthProvider instance;

    public OAuthProviderInjectionProvider(@Context ProviderServices ps) {
        Iterator<OAuthProvider> providers = ps.getProviders(OAuthProvider.class).iterator();

        if (!providers.hasNext()) {
            instance = null;
        } else {
            instance = providers.next();
            if (providers.hasNext()) {
                StringBuilder sb = new StringBuilder("More than one OAuthProvider implementations registered: ");
                sb.append(instance.getClass().getName());
                while (providers.hasNext()) {
                    sb.append(", ").append(providers.next().getClass().getName());
                }
                LOGGER.warning(sb.toString());
            }
        }
    }

    @Override
    public OAuthProvider getValue() {
        return instance;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }

    @Override
    public Injectable getInjectable(ComponentContext cc, Context a, Type t) {
        if ((instance != null) && (t instanceof Class)) {
            Class c = (Class) t;
            if (OAuthProvider.class.isAssignableFrom(c) && c.isInstance(instance)) {
                return this;
            }
        }

        return null;
    }
}
