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

package com.sun.jersey.spi.resource;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 * A provider that manages the creation of resource class instances. A provider
 * instance is specific to a particular class of resource.
 * <p>
 * It is the responsibility of a ResourceProvider to perform injection onto
 * the properties of a resource. If injection is required then declare an 
 * injectable field of the type {@link InjectableProviderContext} annotated 
 * with {@link javax.ws.rs.core.Context} and create a 
 * {@link ResourceClassInjector} instance to be used to perform injection onto
 * the provided resource. This may be performed in the init method.
 */
public interface ResourceProvider {

    /**
     * Specifies the class of the resource that the provider
     * instance will manage access to.
     *
     * @param provider the component provider
     * @param resource the abstract resource
     */
    void init(ComponentProvider provider, AbstractResource resource);
    
    /**
     * Called to obtain an instance of a resource class.
     * 
     * @param provider the component provider
     * @param context the HTTP context
     * @return an initialized instance of the supplied class
     */
    Object getInstance(ComponentProvider provider, HttpContext context);
}