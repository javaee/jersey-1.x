/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl;

import java.net.URI;


import junit.framework.TestCase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.WebApplication;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractResourceTester extends TestCase implements ContainerListener {
    protected static final URI BASE_URI = URI.create("test:/base/");

    protected WebApplication w;
    
    protected AbstractResourceTester(String testName) {
        super(testName);
    }
    
    protected void initiateWebApplication(IoCComponentProviderFactory provider, ResourceConfig c) {
        w = createWebApplication(c, provider);
    }

    protected void initiateWebApplication(IoCComponentProviderFactory provider, Class... classes) {
        w = createWebApplication(provider, classes);
    }
    
    protected void initiateWebApplication(Class... classes) {
        w = createWebApplication(classes);
    }
    
    protected void initiateWebApplication(ResourceConfig c) {
        w = createWebApplication(c);
    }
    
    private WebApplication createWebApplication(Class... classes) {
        return createWebApplication(null, classes);
    }
    
    private WebApplication createWebApplication(IoCComponentProviderFactory provider, Class... classes) {
        ResourceConfig rc = new DefaultResourceConfig(classes);
        
        return createWebApplication(rc, provider);
    }
    
    private WebApplication createWebApplication(ResourceConfig c) {
        return createWebApplication(c, null);
    }
    
    private WebApplication createWebApplication(ResourceConfig c, IoCComponentProviderFactory provider) {
        Object o = c.getProperties().get(
                ResourceConfig.PROPERTY_CONTAINER_NOTIFIER);
        if (o instanceof ContainerNotifier) {
            ContainerNotifier crf = (ContainerNotifier)o;
            crf.addListener(this);
        }

        WebApplicationImpl a = new WebApplicationImpl();
        initiate(c, a);
        a.initiate(c, provider);
        return a;
    }

    protected void initiate(ResourceConfig c, WebApplication a) {}
    
    protected WebResource resource(String relativeUri) {
        return resource(relativeUri, true);
    }
    
    protected WebResource resource(String relativeUri, ClientConfig clientConfig) {
        return resource(relativeUri, true, clientConfig);
    }
    
    protected WebResource resource(String relativeUri, boolean checkStatus) {
        return resource(relativeUri, checkStatus, null);        
    }
    
    protected WebResource resource(String relativeUri, boolean checkStatus, 
            ClientConfig clientConfig) {
        Client c = (clientConfig == null) 
            ? new Client(new TestResourceClientHandler(BASE_URI, w))
            : new Client(new TestResourceClientHandler(BASE_URI, w), clientConfig);

        if (checkStatus) {
            c.addFilter(new ClientFilter() {
                public ClientResponse handle(ClientRequest ro) {
                    ClientResponse r = getNext().handle(ro);
                    assertTrue("Status: " + r.getStatus(), r.getStatus() < 300);
                    return r;
                }
            });
        }
        WebResource r = c.resource(createCompleteUri(BASE_URI, relativeUri));
        
        return r;
    }

    protected interface Closure {
        void f();
    }

    protected <T extends RuntimeException> T catches(Closure c, Class<T> rex) {
        T t = null;
        try {
            c.f();
        } catch(RuntimeException ex) {
            assertTrue(ex.getClass().getName() + " is not assignable to runtime exception " + rex.getName(),
                    ex.getClass().isAssignableFrom(rex));
            t = rex.cast(ex);
        }
        assertNotNull("No exception was caught of class " + rex.getName(), t);
        return t;
    }

    private URI createCompleteUri(URI baseUri, String relativeUri) {
        if (relativeUri.startsWith("/"))
            relativeUri = relativeUri.substring(1);
        
        return URI.create(baseUri.toString() + relativeUri);
    }

    // ContainerListener

    public void onReload() {
        w = w.clone();
    }
}