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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.ContainerProvider;
import com.sun.ws.rest.spi.container.WebApplication;
import org.apache.coyote.Adapter;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class GrizzlyContainerProvider implements ContainerProvider<Adapter> {
    
    public Adapter createContainer(Class<Adapter> type, 
            ResourceConfig resourceConfig, WebApplication application) throws ContainerException {
        if (type != Adapter.class)
            return null;
        
        GrizzlyContainer c = new GrizzlyContainer(application);
        application.initiate(c, resourceConfig);
        return c;
    }
}
