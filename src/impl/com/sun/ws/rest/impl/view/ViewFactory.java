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

package com.sun.ws.rest.impl.view;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.spi.service.ServiceFinder;
import com.sun.ws.rest.spi.view.View;
import com.sun.ws.rest.spi.view.ViewProvider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ViewFactory {
    
    private ViewFactory() {
    }
    
    /**
     * Create a view from a resource.
     * 
     * @param request the HTTP request
     * @param resource the URL to the resource from which the view shall
     *        be created.
     * @return the view.
     */
    public static View createView(HttpRequestContext request, Object it, String resource) throws ContainerException {
        for (ViewProvider vp : ServiceFinder.find(ViewProvider.class)) {
            View v = vp.createView(request, it, resource);
            if (v != null)
                return v;
        }

        return null;
    }
    
}
