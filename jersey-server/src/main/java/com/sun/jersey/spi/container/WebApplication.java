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
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;

/**
 * A Web application that manages a set of resource classes.
 * <p>
 * The web application will dispatch HTTP requests to the matching resource
 * method on the matching resource class.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface WebApplication {
    /**
     *
     * @return true if th web application is initiated, otherwise false.
     */
    boolean isInitiated();

    /**
     * Initiate the Web application.
     * <p>
     * This method can only be called once. Further calls will result in an
     * exception.
     *
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
     * 
     * @param resourceConfig the resource configuration containing the set
     *        of Web resources to be managed by the Web application.
     * @param provider the IoC component provider factory to use, if null the default
     *        component provider factory will be used.
     * @throws IllegalArgumentException if resourceConfig is null.
     * @throws ContainerException if a second or further call to the method 
     *         is invoked.
     */
    void initiate(ResourceConfig resourceConfig, IoCComponentProviderFactory provider)
            throws IllegalArgumentException, ContainerException;
    
    /**
     * Clone the WebApplication instance.
     * <p>
     * A new WebApplication instance will be created that is initiated with
     * the {@link ResourceConfig} and {@link IoCComponentProviderFactory} instances
     * that were used to initiate this WebApplication instance.
     *
     * @return the cloned instance.
     */
    WebApplication clone();
            
    /**
     * Get the message body workers that can be used for getting
     * message body readers and writers. 
     * 
     * @return the message body workers. The return value is 
     *         undefined before the web applicaiton is initialized.
     */
    MessageBodyWorkers getMessageBodyWorkers();

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
     * @param responseWriter the HTTP container response writer.
     * @throws IOException if there is an IO error handling the request.
     */
    void handleRequest(ContainerRequest request, ContainerResponseWriter responseWriter) 
            throws IOException;
    
    /**
     * Handle an HTTP request by dispatching the request to the appropriate
     * matching Web resource that produces the response or otherwise producing 
     * the appropriate HTTP error response.
     * <p>
     * @param request the HTTP container request.
     * @param response the HTTP container response.
     * @throws IOException if there is an IO error handling the request.
     */
    void handleRequest(ContainerRequest request, ContainerResponse response) 
            throws IOException;

    /**
     * Destroy the Web application.
     * <p>
     * This method MUST only be called only once. Calls to <code>handlerRequest</code>
     * MUST not occur while and after this method has been called.
     */
    void destroy();
}