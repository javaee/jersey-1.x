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
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
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
    
    protected void initiateWebApplication(Class... classes) {
        w = createWebApplication(classes);
    }
    
    protected void initiateWebApplication(ResourceConfig c) {
        w = createWebApplication(c);
    }
    
    private WebApplication createWebApplication(Class... classes) {
        return createWebApplication(new HashSet<Class>(Arrays.asList(classes)));
    }
    
    private WebApplication createWebApplication(Set<Class> classes) {
        ResourceConfig rc = new DefaultResourceConfig(
                getMatchingClasses(classes, Path.class));
        rc.getProviderClasses().addAll(
                getMatchingClasses(classes, Provider.class));
        
        return createWebApplication(rc);
    }
    
    private WebApplication createWebApplication(ResourceConfig c) {
        WebApplicationImpl a = new WebApplicationImpl();
        a.initiate(c);
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
    
    private static Set<Class> getMatchingClasses(Set<Class> classes, Class... annotations) {
        Set<Class> s = new HashSet<Class>();
        for (Class c : classes) {
            if (hasAnnotations(c, annotations))
                s.add(c);
        }
        return s;
    }
    
    @SuppressWarnings("unchecked")
    private static boolean hasAnnotations(Class c, Class... annotations) {
        Annotation[] _as = c.getAnnotations();
        for (Class a : annotations) {
            if (c.getAnnotation(a) == null) return false;
        }
        
        return true;
    }    
}
