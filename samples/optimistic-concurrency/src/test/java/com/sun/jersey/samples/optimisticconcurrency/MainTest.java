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

package com.sun.jersey.samples.optimisticconcurrency;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.MediaTypes;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author Naresh (srinivas.bhimisetty@sun.com)
 */
public class MainTest extends TestCase {

    private SelectorThread threadSelector;

    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //start the Grizzly web container and create the client
        threadSelector = Main.startServer();

        Client c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

    /**
     * Test checks that an application.wadl file is present for the resource.
     */
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
        assertTrue("Looks like the expected wadl is not generated",
                serviceWadl.length() > 0);
    }

    /**
     * Test checks that an xml content is shown for the client request to
     * resource "item".
     */
    public void testItemResource() {
        String serviceXml = r.path("item").
                accept(MediaType.APPLICATION_XML).get(String.class);
        assertTrue("Looks like the fiven xml response is not the expected one",
                serviceXml.length() > 0);
    }

    /**
     * Test checks that the initial content seen in the response
     * is the same as what is expected.
     */
    public void testItemContentResource() {
        String itemContent = r.path("item").path("content").
                accept(MediaType.TEXT_PLAIN).get(String.class);
        String expectedContentPrefix = "Today is";
        assertTrue("The response text doesn't start with the expected value",
                itemContent.startsWith(expectedContentPrefix));
    }

    /**
     * Test checks the PUT on resource item/content works,
     * and is allowed only once per content item.
     */
    public void testOnUpdateItemContent() {
        // Create a child WebResource
        WebResource content = r.path("item").path("content");

        String putData = "All play and no REST makes me a dull boy";
        content.path("0").type(MediaType.TEXT_PLAIN)
                .put(putData);
        String contentData = content.get(String.class);
        assertEquals("Data that has been PUT is not the same as what is retrieved",
                putData, contentData);
        // check that the 409 error is seen on a duplicate update
        ClientResponse cr = content.path("0").type(MediaType.TEXT_PLAIN)
                .put(ClientResponse.class , putData);
        int responseStatus = cr.getStatus();
        assertEquals("Expected 409 HTTP error not seen", 409, responseStatus);
    }
    
}
