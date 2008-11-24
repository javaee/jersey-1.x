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
package com.sun.jersey.core.spi.component.ioc;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentProviderFactory;

/**
 * An IoC component provider factory. An implementaton of such a class may be
 * used to support integration with Inversion of Control frameworks such as
 * Spring and Guice.
 * <p>
 * An instance of IoCComponentProviderFactory may be registered with a Client
 * or WebApplication instance on contruction and initialization respectively.
 * <p>
 * When a component, a resource class or provider class, needs to be managed
 * the runtime will defer to the registered IoCComponentProviderFactory instance
 * to obtain a {@link IoCComponentProvider} from which a component instance,
 * of the resource class or provider class, can be obtained.
 * If the component is not supported then a null value may be returned and the
 * runtime will manage the component.
 * <p>
 * Specializations of {@link IoCComponentProvider} must be returned by the 
 * <code>getComponentProvider</code> methods that declare the boundary of
 * responsibility, between the runtime and the underlying IoC framework,
 * for management of a component.
 * <p>
 * If an instance of {@link IoCManagedComponentProvider} is returned then
 * the component is fully managed by the underlying IoC framework, which
 * includes managing the construction, injection and destruction according
 * to the life-cycle declared in the IoC framework's semantics.
 * <p>
 * If an instance of {@link IoCInstantiatedComponentProvider} is returned then
 * the component is instantiated and injected by the underlying IoC framework,
 * but the life-cycle is managed by the runtime according to the life-cycle
 * declared in the runtime's semantics.
 * <p>
 * If an instance of {@link IoCProxiedComponentProvider} is returned then the
 * component is fully managed by the runtime but when an instance is created
 * the underlying IoC framework is deferred to for creating a proxy of the
 * component instance.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface IoCComponentProviderFactory extends ComponentProviderFactory<IoCComponentProvider> {

    /**
     * Get the IoC component provider for a class.
     *
     * @param c the class
     * @return the IoC component provider for the class, otherwise null if the
     *         class is not supported.
     */
    IoCComponentProvider getComponentProvider(Class<?> c);

    /**
     * Get the IoC component provider for a class with additional context.
     * <p>
     * The additional context will be associated with the annotations and
     * optionally an annotated object. For example, a component provider may
     * be requested for a class that is the type of a {@link Field}, or be
     * requested for a class that is the type of a method parameter.
     *
     * @param cc the component context to obtain annotations and
     *        the annotated object (if present).
     * @param c the class
     * @return the IoC component provider for the class, otherwise null if the
     *         class is not supported.
     */
    IoCComponentProvider getComponentProvider(ComponentContext cc, Class<?> c);
}