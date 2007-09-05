/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.spi.container;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.spi.service.ServiceFinder;
import java.util.Iterator;

/**
 * A factory for WebApplication instances. Container providers use this class
 * to obtain an instance of the API runtime.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class WebApplicationFactory {
    
    private WebApplicationFactory() {
    }
    
    /**
     * Create a Web application instance.
     *
     * @return the web application.
     * @throws ContainerException if there is an error creating the Web application.
     */
    public static WebApplication createWebApplication() throws ContainerException {
        for (WebApplicationProvider wap : ServiceFinder.find(WebApplicationProvider.class)) {
            // Use the first provider found
            return wap.createWebApplication();
        }
                
        throw new ContainerException("No WebApplication provider is present");
    }
    
}
