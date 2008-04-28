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

package com.sun.ws.rest.impl.container.jaxws;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.container.WebApplication;
import javax.xml.ws.Provider;

/**
 * Provider container provider.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ProviderContainerProvider implements ContainerProvider<Provider> {
    public ProviderContainerProvider() {
        Class<?> c = Provider.class;
    }
    
    public Provider createContainer(Class<Provider> type, 
            ResourceConfig resourceConfig, WebApplication application) throws ContainerException {
        if (type != Provider.class)
            return null;
        
        ProviderContainer c = new ProviderContainer(application);        
        application.initiate(resourceConfig);
        return c;        
    }
}
