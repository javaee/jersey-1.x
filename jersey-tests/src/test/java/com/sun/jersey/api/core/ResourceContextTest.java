/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.api.core;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.resource.Singleton;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;


/**
 * Test {@link ResourceContext}: resource context must provide access to
 * subresources that can be provided by a custom component provider.<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceContextTest extends AbstractResourceTester {

    public ResourceContextTest(String testName) {
        super( testName );
    }

    @Path("/")
    public static class MyRootResource {
        
        @Context ResourceContext resourceContext;
        
        @Path("singleton")
        public SingletonResource getSingletonResource() {
            return resourceContext.getResource(SingletonResource.class);
        }      
        
        @Path("perrequest")
        public PerRequestResource getPerRequestSubResource() {
            return resourceContext.getResource(PerRequestResource.class);
        }
    }

    @Singleton
    public static class SingletonResource {
        int i;

        @GET
        public String get() {
            i++;
            return Integer.toString(i);
        }
    }

    public static class PerRequestResource {
        int i;

        @GET
        public String get() {
            i++;
            return Integer.toString(i);
        }
    }

    public void testGetResourceFromResourceContext() {
        initiateWebApplication(MyRootResource.class);

        assertEquals("1", resource("/singleton").get(String.class));
        assertEquals("2", resource("/singleton").get(String.class));

        assertEquals("1", resource("/perrequest").get(String.class));
        assertEquals("1", resource("/perrequest").get(String.class));
    }


    @Path("/match")
    public static class MatchResource {

        @Context ResourceContext resourceContext;

        @GET
        @Path("{uri: .+}")
        public String get(@PathParam("uri") URI uri) {
            Object r = resourceContext.matchResource(uri);
            return (r != null) ? r.toString() : "null";
        }

        @GET
        @Path("/class/{class}/{uri: .+}")
        public String get(@PathParam("uri") URI uri, @PathParam("class") String className) {
            Class c = ReflectionHelper.classForName(className);
            Object r = resourceContext.matchResource(uri, c);
            return (r != null) ? r.toString() : "null";
        }

    }

    public void testMatchResourceWithRelativeURI() {
        initiateWebApplication(MatchResource.class, MyRootResource.class);

        assertEquals(resource("/match/singleton").get(String.class),
                resource("/match/singleton").get(String.class));

        String r1 = resource("/match/perrequest").get(String.class);
        String r2 = resource("/match/perrequest").get(String.class);
        assertEquals(r1.substring(0, r1.indexOf('@')),
                r2.substring(0, r2.indexOf('@')));
    }

    public void testMatchResourceWithAbsoluteURI() {
        initiateWebApplication(MatchResource.class, MyRootResource.class);

        assertEquals(resource("/match/test:/base/singleton").get(String.class),
                resource("/match/test:/base/singleton").get(String.class));

        String r1 = resource("/match/test:/base/perrequest").get(String.class);
        String r2 = resource("/match/test:/base/perrequest").get(String.class);
        assertEquals(r1.substring(0, r1.indexOf('@')),
                r2.substring(0, r2.indexOf('@')));
    }

    public void testMatchResourceWithClass() {
        initiateWebApplication(MatchResource.class, MyRootResource.class);

        assertEquals(resource("/match/class/" + SingletonResource.class.getName() + "/singleton").get(String.class),
                resource("/match/class/" + SingletonResource.class.getName() + "/singleton").get(String.class));

        String r1 = resource("/match/class/" + PerRequestResource.class.getName() + "/perrequest").get(String.class);
        String r2 = resource("/match/class/" + PerRequestResource.class.getName() + "/perrequest").get(String.class);
        assertEquals(r1.substring(0, r1.indexOf('@')),
                r2.substring(0, r2.indexOf('@')));
    }
    
    public void testMatchNotFound() {
        initiateWebApplication(MatchResource.class, MyRootResource.class);

        assertEquals("null", resource("/match/foo").get(String.class));
    }


    public void testMatchBaseBaseUri() {
        initiateWebApplication(MatchResource.class, MyRootResource.class);

        catches(new Closure() {
            @Override
            public void f() {
                resource("/match/test:/no-base/singleton").get(String.class);
            }
        }, ContainerException.class);
    }
}
