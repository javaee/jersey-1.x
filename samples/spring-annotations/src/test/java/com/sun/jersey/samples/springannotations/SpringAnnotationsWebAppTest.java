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

package com.sun.jersey.samples.springannotations;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.samples.springannotations.model.Item;
import com.sun.jersey.samples.springannotations.model.Item2;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;
import static org.junit.Assert.*;

/**
 *
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class SpringAnnotationsWebAppTest extends JerseyTest {

    public SpringAnnotationsWebAppTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.samples.springannotations.resources.jerseymanaged")
                .contextPath("spring")
                .contextParam("contextConfigLocation", "classpath:applicationContext.xml")
                .servletClass(SpringServlet.class)
                .contextListenerClass(ContextLoaderListener.class)
                .build());
    }

    /**
     * Test checks that an application.wadl file is generated.
     */
    @Test
    public void doTestApplicationWadl() {
        WebResource webResource = resource();
        String wadl = webResource.path("application.wadl").accept(MediaTypes.WADL)
                .get(String.class);
        assertTrue("Method: doTestApplicationWadl \nMessage: Something wrong, the returned " +
                "WADL's length is not > 0", wadl.length() > 0);
    }

    /**
     * Test checks that a request for the resource "spring-resourced" gives
     * a valid response.
     */
    @Test
    public void doTestSpringResourced() {
        WebResource webResource = resource();
        Item2 item = webResource.path("spring-resourced").accept(MediaType.APPLICATION_XML)
                .get(Item2.class);
        assertEquals("Method: doTestSpringResourced \nMessage: Returned item's value " +
                " does not match the expected one.", "baz", item.getValue());
    }

    /**
     * Test checks that a request for the resource "spring-autowired" gives a
     * valid response.
     */
    @Test
    public void doTestSpringAutowired() {
        WebResource webResource = resource();
        Item2 item = webResource.path("spring-autowired").accept(MediaType.APPLICATION_XML)
                .get(Item2.class);
        assertEquals("Method: doTestSpringAutowired \nMessage: Returned item's value " +
                " does not match the expected one.", "bar", item.getValue());
    }

    /**
     * Test checks that a request for the resource "jersey-autowired" gives
     * a valid response.
     */
    @Test
    public void doTestJerseyAutowired() {
        WebResource webResource = resource();
        Item item = webResource.path("jersey-autowired").accept(MediaType.APPLICATION_XML)
                .get(Item.class);
        assertEquals("Method: doTestJerseyAutowired \nMessage: Returned item's value " +
                " does not match the expected one.", "foo", item.getValue());
    }

}