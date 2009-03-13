/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.bookstore.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.samples.bookstore.resources.glassfish.GlassFishFacade;
import junit.framework.TestCase;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * A base class for test cases which boots up a GlassFish server for in container testing of RESTful resources
 *
 * @author James Strachan
 * @author Naresh
 */
public class TestSupport extends TestCase {

    protected static final URI BASE_URI = getBaseURI();

    protected WebResource baseResource;
    protected WebContainerFacade webContainer = createWebContainerFacade();

    protected static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    protected static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
                path("bookstore").build();
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        webContainer.setUp();

        Client client = Client.create();
        baseResource = client.resource(BASE_URI);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        webContainer.tearDown();
    }

    protected WebContainerFacade createWebContainerFacade() {
        // TODO we could use system properties or something to choose
        return new GlassFishFacade(BASE_URI);
    }


    protected void assertHtmlResponse(String response) {
        assertNotNull("No text returned!", response);

        assertResponseContains(response, "<html>");
        assertResponseContains(response, "</html>");
    }

    protected void assertResponseContains(String response, String text) {
        assertTrue("Response should contain " + text + " but was: " + response, response.contains(text));
    }
}