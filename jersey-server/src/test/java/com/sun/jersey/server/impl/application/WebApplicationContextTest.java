/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.application;

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import javax.ws.rs.core.UriBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin Matula
 */
public class WebApplicationContextTest {

    private WebApplicationImpl wa = new WebApplicationImpl();

    public WebApplicationContextTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetMatchedURIs_0args() {
        WebApplicationContext instance = new WebApplicationContext(wa,
                new ContainerRequest(wa, "GET", UriBuilder.fromPath("http://localhost/").build(),
                    UriBuilder.fromPath("http://localhost/one%20two/three%20four").build(), new InBoundHeaders(), null)
                , null);
        instance.pushRightHandPathLength(12);
        assertEquals(instance.getMatchedURIs(true).get(0), instance.getMatchedURIs().get(0));
        assertNotSame(instance.getMatchedURIs(false).get(0), instance.getMatchedURIs().get(0));
    }

    @Test
    public void testGetMatchedURIs_boolean() {
        WebApplicationContext instance = new WebApplicationContext(wa,
                new ContainerRequest(wa, "GET", UriBuilder.fromPath("http://localhost/").build(),
                    UriBuilder.fromPath("http://localhost/one%20two/three%20four").build(), new InBoundHeaders(), null)
                , null);
        instance.pushRightHandPathLength(12);
        assertEquals("one%20two/", instance.getMatchedURIs(false).get(0));
        assertEquals("one two/", instance.getMatchedURIs(true).get(0));
    }
}