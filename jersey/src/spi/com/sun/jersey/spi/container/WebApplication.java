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

package com.sun.jersey.spi.container;


import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentProvider;
import javax.ws.rs.ext.MessageBodyWorkers;

/**
 * A Web application that manages a set of Web resource.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface WebApplication {
    /**
     * Initiate the Web application.
     * <p>
     * This method can only be called once. Further calls will result in an
     * exception.
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @throws IllegalArgumentException if resourceConfig is null.
     * @throws ContainerException if a second or further call to the method 
     *         is invoked.
     */
    void initiate(ResourceConfig resourceConfig) 
            throws IllegalArgumentException, ContainerException;
    
    /**
     * Initiate the Web application.
     * <p>
     * This method can only be called once. Further calls will result in an
     * exception.
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @param provider the component provider to use, if null the default
     *        component provider will be used.
     * @throws IllegalArgumentException if resourceConfig is null.
     * @throws ContainerException if a second or further call to the method 
     *         is invoked.
     */
    void initiate(ResourceConfig resourceConfig, ComponentProvider provider) 
            throws IllegalArgumentException, ContainerException;
    
    /**
     * Clone the WebApplication instance.
     * <p>
     * A new WebApplication instance will be created that is initiated with
     * the {@link ResourceConfig} and {@link ComponentProvider} instances
     * that were used to initiate this WebApplication instance.
     *
     * @return the cloned instance.
     */
    WebApplication clone();
            
    /**
     * Get the extended message body workers that can be used for getting
     * message body readers and writers. 
     * 
     * @return the extended message body workers. The return value is 
     * undefined before the web applicaiton is initialized.
     */
    ExtendedMessageBodyWorkers getMessageBodyWorkers();

    /**
     * Get the component provider that can be used for instantiating
     * components other than resource classes.
     * 
     * @return the component provider. The return value is 
     * undefined before the web applicaiton is initialized.
     */
    ComponentProvider getComponentProvider();

    /**
     * Get the component provider that can be used for instantiating
     * resource classes.
     * 
     * @return the component provider. The return value is 
     * undefined before the web applicaiton is initialized.
     */
    ComponentProvider getResourceComponentProvider();
    
    /**
     * Add an injectable provider that provides injectable values.
     * 
     * @param ip the injectable provider
     */
    void addInjectable(InjectableProvider<?, ?> ip);
        
    /**
     * Get an instance of {@link HttpContext} that is a proxy to
     * a thread local instance of {@link HttpContext}.
     * 
     * @return the thread local instance of HttpContext.
     */
    HttpContext getThreadLocalHttpContext();
    
    /**
     * Handle an HTTP request by dispatching the request to the appropriate
     * matching Web resource that produces the response or otherwise producing 
     * the appropriate HTTP error response.
     * <p>
     * @param request the HTTP container request.
     * @param response the HTTP container response.
     * @throws ContainerException if there is an error that the container 
     * should manage.
     */
    void handleRequest(ContainerRequest request, ContainerResponse response)
    throws ContainerException;    
}