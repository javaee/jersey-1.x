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
package com.sun.jersey.spring.tests.inheritence;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spring.tests.AbstractTest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.springframework.stereotype.Component;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Test
public class InheritenceTest extends AbstractTest {

    @Path("a")
    @Component
    public static class A {
        @GET
        public String get() {
            return "a";
        }

        @Path("sub")
        public ASub getSub() {
            return new ASub();
        }
    }

    @Path("b")
    @Component
    public static class B extends A {
        @GET
        @Override
        public String get() {
            return "b";
        }

        @Path("sub")
        @Override
        public BSub getSub() {
            return new BSub();
        }
    }

    @Path("b2")
    @Component
    public static class B2 extends A {
        @GET
        @Override
        public String get() {
            return "b2";
        }

        @Path("sub")
        @Override
        public B2Sub getSub() {
            return new B2Sub();
        }
    }

    @Path("c")
    @Component
    public static class C extends B {
        @GET
        @Override
        public String get() {
            return "c";
        }

        @Path("sub")
        @Override
        public CSub getSub() {
            return new CSub();
        }
    }

    @Component
    public static class ASub {
        @GET
        public String get() {
            return "asub";
        }
    }

    @Component
    public static class BSub extends ASub {
        @GET
        @Override
        public String get() {
            return "bsub";
        }
    }

    @Component
    public static class B2Sub extends ASub {
        @GET
        @Override
        public String get() {
            return "b2sub";
        }
    }

    @Component
    public static class CSub extends BSub {
        @GET
        @Override
        public String get() {
            return "csub";
        }
    }

    @Test
    public void testInheritence() {
        start();

        WebResource r = resource("a");
        Assert.assertEquals("a", r.get(String.class));

        r = resource("b");
        Assert.assertEquals("b", r.get(String.class));

        r = resource("b2");
        Assert.assertEquals("b2", r.get(String.class));

        r = resource("c");
        Assert.assertEquals("c", r.get(String.class));
    }

    @Test
    public void testSubInheritence() {
        start();

        WebResource r = resource("a/sub");
        Assert.assertEquals("asub", r.get(String.class));

        r = resource("b/sub");
        Assert.assertEquals("bsub", r.get(String.class));

        r = resource("b2/sub");
        Assert.assertEquals("b2sub", r.get(String.class));

        r = resource("c/sub");
        Assert.assertEquals("csub", r.get(String.class));
    }
}
