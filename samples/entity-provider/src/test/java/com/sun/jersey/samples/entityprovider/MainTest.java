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
package com.sun.jersey.samples.entityprovider;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Naresh
 */
public class MainTest extends JerseyTest {

    public MainTest()throws Exception {
        super();
        ApplicationDescriptor appDescriptor = new ApplicationDescriptor();
        appDescriptor.setRootResourcePackageName("com.sun.jersey.samples.entityprovider");
        super.setupTestEnvironment(appDescriptor);
    }

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    @Test
    public void testApplicationWadl() {
        String serviceWadl = webResource.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
        assertTrue(serviceWadl.length() > 0);
    }

    /**
     * Test checks that a request to properties resource gives back
     * a list of properties that contains the "java.class.path"
     * property.
     */
    @Test
    public void testPropertiesResource() throws IOException {
        String sProperties = webResource.path("properties").accept(MediaType.TEXT_PLAIN).get(String.class);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(sProperties.getBytes()));
        assertNotNull("Properties does not contain 'java.class.path' property", 
                properties.getProperty("java.class.path"));
    }

    /**
     * Test checks that a GET request on "data" resource gives back a reponse
     * with status "OK".
     */
    @Test
    public void testGetOnDataResource() {
        ClientResponse response = webResource.path("data").accept(MediaType.TEXT_HTML).get(ClientResponse.class);
        assertEquals("Request for data doesn't give expected response.",
                Response.Status.OK, response.getResponseStatus());
    }

    /**
     * Test checks that a POST on "data" resource adds the submitted data to
     * the maintained map.
     */
    @Test
    public void testPostOnDataResource() {
        Form formData = new Form();
        formData.add("name", "testName");
        formData.add("value", "testValue");
        ClientResponse response = webResource.path("data").type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formData);
        assertEquals(Response.Status.OK, response.getResponseStatus());
        String responseMsg = webResource.path("data").type(MediaType.TEXT_HTML).get(String.class);
        assertTrue("Submitted data did not get added to the list...",
                responseMsg.contains("testName") && responseMsg.contains("testValue"));

    }
}