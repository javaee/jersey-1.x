/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class QualitySourceTest extends AbstractResourceTester {
    
    public QualitySourceTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @Produces("application/baz")
        @GET
        public String doGetBaz() {
            return "baz";
        }

        @Produces("application/bar;qs=2")
        @GET
        public String doGetBar() {
            return "bar";
        }

        @Produces("application/foo;qs=3")
        @GET
        public String doGetFoo() {
            return "foo";
        }
    }

    public void testAcceptGet() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");

        String s = r.accept("application/foo").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/bar").get(String.class);
        assertEquals("bar", s);

        s = r.accept("application/baz").get(String.class);
        assertEquals("baz", s);

        s = r.accept("application/bar, application/foo").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo, application/bar").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/baz, application/bar, application/foo").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo, application/bar, application/baz").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/baz, application/bar").get(String.class);
        assertEquals("bar", s);
    }

    public void testAcceptWithQualityGet() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");

        String s = r.accept("application/bar, application/foo;q=0.8").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo;q=0.8, application/bar").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/baz, application/bar;q=0.6, application/foo;q=0.8").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo;q=0.8, application/bar;q=0.6, application/baz").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/baz, application/bar;q=0.6").get(String.class);
        assertEquals("bar", s);
    }

    // TODO test is failing
    public void testWildCard() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");

        String s = r.accept("*/*").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/*").get(String.class);
        assertEquals("foo", s);
    }
}
