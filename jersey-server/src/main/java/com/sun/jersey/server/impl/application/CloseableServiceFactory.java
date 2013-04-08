/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.server.impl.application;

import com.sun.jersey.spi.CloseableService;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;

/**
 *
 * @author paulsandoz
 */
public class CloseableServiceFactory implements
        InjectableProvider<Context, Type>, Injectable<CloseableService>, CloseableService {
    private static final Logger LOGGER = Logger.getLogger(CloseableServiceFactory.class.getName());

    private final HttpContext context;

    public CloseableServiceFactory(@Context HttpContext context) {
        this.context = context;
    }

    // InjectableProvider

    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }

    public Injectable getInjectable(ComponentContext ic, Context a, Type c) {
        if (c != CloseableService.class)
           return null;

        return this;
    }

    // Injectable

    public CloseableService getValue() {
        return this;
    }

    // CloseableService
    
    public void add(Closeable c) {
        Set<Closeable> s = (Set<Closeable>)context.getProperties().
                get(CloseableServiceFactory.class.getName());
        if (s == null) {
            s = new HashSet<Closeable>();
            context.getProperties().put(CloseableServiceFactory.class.getName(), s);
        }

        s.add(c);
    }

    public void close(HttpContext context) {
        Set<Closeable> s = (Set<Closeable>)context.getProperties().
                get(CloseableServiceFactory.class.getName());
        if (s != null) {
            for (Closeable c : s) {
                try {
                    c.close();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Unable to close", ex);
                }
            }
        }
    }
}
