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

package com.sun.jersey.samples.servlet.resources;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Naresh
 */
public class MasterResourceBeanTest extends JerseyTest {

    public MasterResourceBeanTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.samples.servlet.resources")
                .contextPath("SimpleServlet")
                .servletPath("/resources")
                .build());
    }


    /**
     * Test that a request to the resource path "/start" redirects to the html page.
     */
    @Test
    public void doTestStartPage() {
       WebResource webResource = resource();
        int responseStatus = webResource.path("start")
                .accept(MediaType.TEXT_HTML).head().getStatus();
        assertEquals(200, responseStatus);
        String responseHtml = webResource.path("start")
                .accept(MediaType.TEXT_HTML).get(String.class);
        // check for the various components in the html page
        assertTrue(responseHtml.contains("Select Resource to test:"));
        assertTrue(responseHtml.contains("<select name=\"ressel\" id=\"resourceId\">"));
    }

    /**
     * Test that the request for resource 1 gives appropriate response.
     */
    @Test
    public void doTestResource1Page() {
        WebResource webResource = resource();
        int responseStatus = webResource.path("resource1")
                .accept(MediaType.TEXT_PLAIN).head().getStatus();
        assertEquals("Response status 200 not found for request to resource 1", 200, responseStatus);
        String responseText = webResource.path("resource1")
                .accept(MediaType.TEXT_PLAIN).get(String.class);
        String expectedText = "Hello World from resource 1 in servlet: 'com.sun.jersey.samples.servlet.resources.MyApplication', path: '/resources'";
        // check that the expected reponse is seen
        assertEquals("Expected response not seen for the GET on resource 1", expectedText, responseText);
    }

    /**
     * Test the request for resource 2 gives the appropriate reponse.
     */
    @Test
    public void doTestResource2Page() {
        WebResource webResource = resource();
        int responseStatus = webResource.path("resource2")
                .accept(MediaType.TEXT_PLAIN).head().getStatus();
        assertEquals("Response status 200 not found for request to resource 2", 200, responseStatus);
        String responseText = webResource.path("resource2")
                .accept(MediaType.TEXT_PLAIN).get(String.class);
        String expectedText = "Hello World from resource 2";
        // check that the expected response is seen
        assertEquals("Expected response not seen for the GET on resource 2", expectedText, responseText);
    }

    /**
     * Test the request for resource 3 with different values for the query param "rep"
     * gives the appropriate response.
     */
    @Test
    public void doTestResource3Page() {
        String arg1 = "firstArg";
        String arg2 = "secondArg";
        String expectedResponseWithRep0 = "<pre>Received args: ";
        String expectedResponseWithRep1 = "representation: StringRepresentation: arg1: ";

        WebResource webResource = resource();

        //test with rep=0

        int responseStatus = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "0").head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep=0", 200, responseStatus);
        String responseText = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "0").get(String.class);
        assertTrue("Expected reponse not seen with query param '?rep=0'",
                responseText.startsWith(expectedResponseWithRep0));

        // test with rep=1
        responseStatus = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "1").head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep=1", 200, responseStatus);
        responseText = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "1").get(String.class);
        assertTrue("Expected reponse not seen with query param '?rep=1'",
                responseText.startsWith(expectedResponseWithRep1));

        // test with rep=2
        responseStatus = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "2").head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep=2", 200, responseStatus);
        Form f = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "2").get(Form.class);
        assertEquals("FormURLEncodedRepresentation", f.getFirst("representation"));
        assertEquals("Master Duke", f.getFirst("name"));
        assertEquals("male", f.getFirst("sex"));
        assertEquals("firstArg", f.getFirst("arg1"));
        assertEquals("secondArg", f.getFirst("arg2"));

        // test with rep>3
        responseStatus = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "4").head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep>3", 200, responseStatus);
        responseText = webResource.path("resource3")
                .path(arg1).path(arg2).queryParam("rep", "4").get(String.class);
        assertTrue("Expected reponse not seen with query param 'rep>3'",
                responseText.startsWith(expectedResponseWithRep0));
    }
}