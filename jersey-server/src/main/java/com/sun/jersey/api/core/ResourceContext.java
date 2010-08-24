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
package com.sun.jersey.api.core;

import com.sun.jersey.api.container.ContainerException;
import java.net.URI;
import javax.ws.rs.core.Context;

/**
 * The resource context provides access to instances of resource classes.
 * <p>
 * This interface can be injected using the {@link Context} annotation.
 * <p>
 * The resource context can be utilized when instances of managed resource
 * classes are to be returned by sub-resource locator methods. Such instances
 * will be injected and managed within the declared scope just like instances
 * of root resource classes.
 * <p>
 * The resource context can be utilized when matching of URIs are
 * required, for example when validating URIs sent in a request entity.
 * Note that application functionality may be affected as the matching
 * process will result in the construction or sharing of previously constructed
 * resource classes that are in scope of the HTTP request, and the invocation of
 * matching sub-resource locator methods. No resource methods wll be invoked.
 *
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @author Paul.Sandoz@Sun.Com
 */
public interface ResourceContext {

    /**
     * Match a URI to URI information.
     * <p>
     * If the URI is relative then the base URI of the application will be
     * used to resolve the relative URI to an absolute URI.
     * If the URI is absolute then it must be relative to the base URI of the
     * application.
     * 
     * @param u the URI.
     * @return the URI information, otherwise null if the URI cannot be matched.
     * @throws ContainerException if there is an error when matching.
     */
    ExtendedUriInfo matchUriInfo(URI u) throws ContainerException;

    /**
     * Match a URI to a resource instance.
     * <p>
     * If the URI is relative then the base URI of the application will be
     * used to resolve the relative URI to an absolute URI.
     * If the URI is absolute then it must be relative to the base URI of the
     * application.
     *
     * @param u the URI.
     * @return the resource instance, otherwise null if the URI cannot be
     *         matched.
     * @throws ContainerException if there is an error when matching.
     */
    Object matchResource(URI u) throws ContainerException;

    /**
     * Match a URI to a resource instance.
     * <p>
     * If the URI is relative then the base URI of the application will be
     * used to resolve the relative URI to an absolute URI.
     * If the URI is absolute then it must be relative to the base URI of the
     * application.
     * 
     * @param <T> the type of the resource.
     * @param u the URI.
     * @param c the resource class.
     * @return the resource instance, otherwise null if the URI cannot be
     *         matched.
     * @throws ContainerException if there is an error when matching.
     * @throws ClassCastException if the resource instance cannot be cast to
     *         <code>c</code>.
     */
    <T> T matchResource(URI u, Class<T> c) throws ContainerException, ClassCastException;
    
    /**
     * Provides an instance of the given resource class.
     * 
     * @param <T> the type of the resource class
     * @param c the resource class
     * @return an instance if it could be resolved, otherwise null.
     * @throws com.sun.jersey.api.container.ContainerException if the resource
     *         class cannot be found.
     */
    <T> T getResource(Class<T> c) throws ContainerException;   
}