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

package com.sun.ws.rest.impl;

import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.client.RequestOutBound;
import com.sun.ws.rest.impl.client.ResponseInBound;
import com.sun.ws.rest.impl.application.WebApplicationImpl;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResourceProxyFilter;
import com.sun.ws.rest.spi.container.WebApplication;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractResourceTester extends TestCase {
    protected static final URI BASE_URI = URI.create("/base/");

    protected WebApplication w;
    
    protected AbstractResourceTester(String testName) {
        super(testName);
    }
    
    protected void initiateWebApplication(Class... resources) {
        w = createWebApplication(resources);
    }
    
    protected void initiateWebApplication(ResourceConfig c) {
        w = createWebApplication(c);
    }
    
    protected WebApplication createWebApplication(Class... resources) {
        return createWebApplication(new HashSet<Class>(Arrays.asList(resources)));
    }
    
    protected WebApplication createWebApplication(Set<Class> resources) {
        return createWebApplication(new DefaultResourceConfig(resources));
    }
    
    protected WebApplication createWebApplication(ResourceConfig c) {
        WebApplicationImpl a = new WebApplicationImpl();
        a.initiate(null, c);
        return a;
    }

    protected ResourceProxy resourceProxy(String relativeUri) {
        return resourceProxy(relativeUri, true);
    }
    
    protected ResourceProxy resourceProxy(String relativeUri, boolean checkStatus) {
        ResourceProxy r = new TestResourceProxy(
                createCompleteUri(BASE_URI, relativeUri), BASE_URI, 
                w);
        if (checkStatus) {
            r.addFilter(new ResourceProxyFilter() {
                public ResponseInBound invoke(URI u, String method, RequestOutBound ro) {
                    ResponseInBound r = getNext().invoke(u, method, ro);
                    if (r.hasEntity()) {
                        assertEquals(200, r.getStatus());
                    } else {
                        assertEquals(204, r.getStatus());
                    }
                    return r;
                }
            });
        }
        
        return r;
    }
    
    private URI createCompleteUri(URI baseUri, String relativeUri) {
        if (relativeUri.startsWith("/"))
            relativeUri = relativeUri.substring(1);
        
        return URI.create(baseUri.toString() + relativeUri);
    }
}
