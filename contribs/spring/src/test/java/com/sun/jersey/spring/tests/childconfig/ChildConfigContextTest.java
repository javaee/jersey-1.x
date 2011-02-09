/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.spring.tests.childconfig;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.spring.tests.AbstractTest;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Test
public class ChildConfigContextTest extends AbstractTest {

    @Path("app")
    public static class ApplicationConfigResource {

        @GET
        public String get() {
            return "app";
        }
    }

    @Path("child")
    public static class ChildConfigResource {

        @GET
        public String get() {
            return "child";
        }
    }

    @Path("child2")
    public static class ChildConfig2Resource {

        @GET
        public String get() {
            return "child2";
        }
    }

    @Test
    public void testApplicationConfig() {
        start();

        WebResource r = resource("app");
        Assert.assertEquals("app", r.get(String.class));

        r = resource("child");

        ClientResponse cr = r.get(ClientResponse.class);
        Assert.assertEquals(404, cr.getStatus());
    }

    @Test
    public void testChildConfig() {
        String clientConfig =  this.getClass().getName();
        clientConfig = clientConfig.replace(".", "/") + "-client-config.xml";

        Map<String, String> m = new HashMap<String, String>();
        m.put(SpringServlet.CONTEXT_CONFIG_LOCATION, "classpath:" + clientConfig);
        start(m);

        WebResource r = resource("app");
        Assert.assertEquals("app", r.get(String.class));

        r = resource("child");
        Assert.assertEquals("child", r.get(String.class));
    }

    @Test
    public void testChildConfig2() {
        String clientConfig = "classpath:" + this.getClass().getName().replace(".", "/") + "-client-config.xml";
        clientConfig += " classpath:" + this.getClass().getName().replace(".", "/") + "-client-config2.xml";

        Map<String, String> m = new HashMap<String, String>();
        m.put(SpringServlet.CONTEXT_CONFIG_LOCATION, clientConfig);
        start(m);

        WebResource r = resource("app");
        Assert.assertEquals("app", r.get(String.class));

        r = resource("child");
        Assert.assertEquals("child", r.get(String.class));

        r = resource("child2");
        Assert.assertEquals("child2", r.get(String.class));
    }

}
