/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.impl.lifecycle;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.Singleton;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Context;

/**
 *
 * @author Marc Hadley
 */
public class SingletonLifecycleTest extends AbstractResourceTester {
            
    public SingletonLifecycleTest(String testName) {
        super(testName);
    }
        
    @Path("/")
    @Singleton
    public static class PostConstructResource {
        private int count;

        @Context HttpContext hc;

        public PostConstructResource() {
            this.count = 0;
        }

        @PostConstruct
        public void postConstruct() {
            count++;
            assertNotNull(hc);
        }

        @GET
        public String doGet() {
            return Integer.toString(count);
        }
    }
    
    public void testPostConstructResource() {
        initiateWebApplication(PostConstructResource.class);
        WebResource r = resource("/");
        assertEquals("1", r.get(String.class));
        assertEquals("1", r.get(String.class));
        assertEquals("1", r.get(String.class));
    }

    @Path("/")
    @Singleton
    public static class PreDestroyResource {
        File f;

        public PreDestroyResource() throws IOException {
            f = File.createTempFile("jersey", null);
        }

        @GET
        public String getFileName() {
            return f.getAbsolutePath();
        }

        @PreDestroy
        public void preDestroy() {
            assertTrue(f.exists());
            f.delete();
        }
    }
    
    public void testPreDestroyResource() {
        initiateWebApplication(PreDestroyResource.class);
        WebResource r = resource("/");
        String s = r.get(String.class);
        w.destroy();
        
        File f = new File(s);
        assertFalse(f.exists());
    }

    @Path("/")
    @Singleton
    public static class ReferredToResource {
        @Path("sub")
        public ReferencingOfResource get() {
            return new ReferencingOfResource();
        }
    }

    public static class ReferencingOfResource {
        @GET
        public String get(@Context ResourceContext rc) {
            ReferredToResource r1 = rc.getResource(ReferredToResource.class);
            ReferredToResource r2 = rc.getResource(ReferredToResource.class);
            assertEquals(r1, r2);
            return "GET";
        }
    }
    
    public void testReferredToResource() {
        initiateWebApplication(ReferredToResource.class);
        WebResource r = resource("/sub");
        assertEquals("GET", r.get(String.class));
    }
}