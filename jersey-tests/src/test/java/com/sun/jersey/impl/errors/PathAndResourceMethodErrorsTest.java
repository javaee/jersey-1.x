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
package com.sun.jersey.impl.errors;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.inject.Errors;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PathAndResourceMethodErrorsTest extends AbstractResourceTester {
    
    public PathAndResourceMethodErrorsTest(String testName) {
        super( testName );
    }

    private Errors.ErrorMessagesException catches(Closure c) {
        return catches(c, Errors.ErrorMessagesException.class);
    }

    @Path("/{")
    public static class PathErrorsResource {
        @Path("/{")
        @GET
        public String get() { return null; }

        @Path("/{sub")
        public Object sub() { return null; }
    }

    public void testPathErrors() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(PathErrorsResource.class);
            }
        }).messages;

        assertEquals(3, messages.size());
    }

    @Path("/{one}")
    public static class PathErrorsOneResource {
    }

    @Path("/{two}")
    public static class PathErrorsTwoResource {
    }

    @Path("/{three}")
    public static class PathErrorsThreeResource {
    }

    public void testConflictingRootResourceErrors() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                ResourceConfig rc = new DefaultResourceConfig(PathErrorsOneResource.class, PathErrorsTwoResource.class);
                rc.getSingletons().add(new PathErrorsThreeResource());
                rc.getExplicitRootResources().put("/{four}", PathErrorsOneResource.class);
                rc.getExplicitRootResources().put("/{five}", new PathErrorsThreeResource());

                initiateWebApplication(rc);
            }
        }).messages;

        assertEquals(4, messages.size());
    }

    public void testConflictingRootResourceErrors2() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                ResourceConfig rc = new DefaultResourceConfig();
                rc.getExplicitRootResources().put("/{one}", PathErrorsOneResource.class);
                rc.getExplicitRootResources().put("/{one}/", new PathErrorsThreeResource());

                initiateWebApplication(rc);
            }
        }).messages;

        assertEquals(1, messages.size());
    }

    @Path("/")
    public static class AmbiguousResourceMethodsGET {

        @GET
        public String get1() { return null; }

        @GET
        public String get2() { return null; }

        @GET
        public String get3() { return null; }
    }

    public void testAmbiguousResourceMethodGET() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousResourceMethodsGET.class);
            }
        }).messages;

        assertEquals(2, messages.size());
    }

    @Path("/")
    public static class AmbiguousResourceMethodsProducesGET {

        @GET
        @Produces("application/xml")
        public String getXml() { return null; }

        @GET
        @Produces("text/plain")
        public String getText1() { return null; }

        @GET
        @Produces("text/plain")
        public String getText2() { return null; }

        @GET
        @Produces("text/plain, image/png")
        public String getText3() { return null; }
    }

    public void testAmbiguousResourceMethodsProducesGET() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousResourceMethodsProducesGET.class);
            }
        }).messages;

        assertEquals(2, messages.size());
    }

    @Path("/")
    public static class AmbiguousResourceMethodsConsumesPUT {

        @PUT
        @Consumes("application/xml")
        public void put1(Object o) { }

        @PUT
        @Consumes({"text/plain", "image/jpeg"})
        public void put2(Object o) { }

        @PUT
        @Consumes("text/plain")
        public void put3(Object o) { }
    }

    public void testAmbiguousResourceMethodsConsumesPUT() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousResourceMethodsConsumesPUT.class);
            }
        }).messages;

        assertEquals(1, messages.size());
    }


    @Path("/")
    public static class AmbiguousSubResourceMethodsGET {

        @Path("{one}")
        @GET
        public String get1() { return null; }

        @Path("{seven}")
        @GET
        public String get2() { return null; }

        @Path("{million}")
        @GET
        public String get3() { return null; }

        @Path("{million}/")
        @GET
        public String get4() { return null; }
    }

    public void testAmbiguousSubResourceMethodsGET() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousSubResourceMethodsGET.class);
            }
        }).messages;

        assertEquals(3, messages.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceMethodsProducesGET {

        @Path("x")
        @GET
        @Produces("application/xml")
        public String getXml() { return null; }

        @Path("x")
        @GET
        @Produces("text/plain")
        public String getText1() { return null; }

        @Path("x")
        @GET
        @Produces("text/plain")
        public String getText2() { return null; }

        @Path("x")
        @GET
        @Produces("text/plain, image/png")
        public String getText3() { return null; }
    }

    public void testAmbiguousSubResourceMethodsProducesGET() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousSubResourceMethodsProducesGET.class);
            }
        }).messages;

        assertEquals(2, messages.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceLocatorsResource {

        @Path("{one}")
        public Object l1() { return null; }

        @Path("{two}")
        public Object l2() { return null; }
    }

    public void testAmbiguousSubResourceLocatorsResource() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousSubResourceLocatorsResource.class);
            }
        }).messages;

        assertEquals(1, messages.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceLocatorsWithSlashResource {

        @Path("{one}")
        public Object l1() { return null; }

        @Path("{two}/")
        public Object l2() { return null; }
    }

    public void testAmbiguousSubResourceLocatorsWithSlashResource() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(AmbiguousSubResourceLocatorsWithSlashResource.class);
            }
        }).messages;

        assertEquals(1, messages.size());
    }
}
