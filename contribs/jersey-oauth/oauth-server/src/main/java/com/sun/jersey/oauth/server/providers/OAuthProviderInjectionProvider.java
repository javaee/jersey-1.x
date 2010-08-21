/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.oauth.server.providers;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/** Provider that handles the injection of the application-specific {@link OAuthProvider}
 * implementation.
 *
 * @author Martin Matula
 */
@Provider
public class OAuthProviderInjectionProvider implements Injectable<OAuthProvider>, InjectableProvider<Context, Type> {
    private final OAuthProvider instance;

    public OAuthProviderInjectionProvider(@Context ResourceConfig rc) {
        OAuthProvider provider = null;
        for (Object obj : rc.getProviderSingletons()) {
            if (obj instanceof OAuthProvider) {
                if (provider == null) {
                    provider = (OAuthProvider) obj;
                } else {
                    Logger.getLogger(OAuthProviderInjectionProvider.class.getName()).log(Level.WARNING, "Multiple instances of OAuthProvider registered. Using: " + provider.getClass().getName());
                    break;
                }
            }
        }
        if (provider == null) {
            for (Class cls : rc.getProviderClasses()) {
                if (OAuthProvider.class.isAssignableFrom(cls)) {
                    try {
                        if (provider == null) {
                            provider = (OAuthProvider) cls.newInstance();
                        } else {
                            Logger.getLogger(OAuthProviderInjectionProvider.class.getName()).log(Level.WARNING, "Multiple implementations of OAuthProvider registered. Using: " + provider.getClass().getName());
                            break;
                        }
                    } catch (InstantiationException ex) {
                        Logger.getLogger(OAuthProviderInjectionProvider.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(OAuthProviderInjectionProvider.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        if (provider == null) {
            throw new RuntimeException("No implementation of OAuthProvider class registered");
        } else {
            instance = provider;
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
    public Injectable getInjectable(ComponentContext cc, Context a, Type c) {
        if (OAuthProvider.class == c) {
            return this;
        } else {
            return null;
        }
    }
}
