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
package com.sun.jersey.test.functional.errormapping;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author naresh
 */
public class ErrorPageMappingTest extends JerseyTest {

    public ErrorPageMappingTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.test.functional.errormapping")
                .contextPath("exceptions").build());

    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new ExternalTestContainerFactory();
    }

    @Before
    public void beforeClass() throws Exception {
        super.setUp();
    }

    @After
    public void afterClass() throws Exception {
        super.tearDown();
    }

    @Test
    public void testNormal() {
        WebResource webResource = resource();
        String response = webResource.path("webresources").path("myresource")
                .accept("text/plain").get(String.class);
        assertEquals("Expected response not seen.", "Hi there!", response);
    }

    @Test
    public void testSecurityExceptionErrorPage() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("webresources")
                .path("myresource")
                .path("runtime")
                .queryParam("ex", "java.lang.SecurityException")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the SecurityException.jsp error pag",
                responseMsg.contains("SecurityException error page"));
        assertTrue("Does not catch the expected java.lang.SecurityException",
                responseMsg.contains("Exception: java.lang.SecurityException"));
    }

    @Test
    public void testCheckedExceptionErrorPageForIOException() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("webresources")
                .path("myresource")
                .path("checked")
                .path("ioexception")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the CheckedException.jsp error pag",
                responseMsg.contains("Checked Exception error page"));
        assertTrue("Does not catch the expected java.io.IOException",
                responseMsg.contains("Exception: java.io.IOException"));
    }

    @Test
    public void testCheckedExceptionErrorPageForMyException() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("webresources")
                .path("myresource")
                .path("checked")
                .path("myexception")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the CheckedException.jsp error pag",
                responseMsg.contains("Checked Exception error page"));
        assertTrue("Does not catch the expected com.sun.jersey.test.functional.errormapping.MyResource$MyException",
                responseMsg.contains("Exception: com.sun.jersey.test.functional.errormapping.MyResource$MyException"));
    }

    @Test
    public void testCheckedExceptionErrorPageForMyMappedException() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("webresources")
                .path("myresource")
                .path("checked")
                .path("mymappedexception")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the desired error page",
                responseMsg
                .contains("Jersey mapped exception: com.sun.jersey.test.functional.errormapping.MyResource$MyMappedException"));
    }

    @Test
    public void testCheckedExceptionErrorPageForMyMappedRuntimeException() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("webresources")
                .path("myresource")
                .path("checked")
                .path("mymappedruntimeexception")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the desired error page",
                responseMsg
                .contains("Jersey mapped runtime exception: com.sun.jersey.test.functional.errormapping.MyResource$MyMappedRuntimeException"));
    }

    @Test
    public void testCheckedExceptionErrorPageForMyMappedThrowingException() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("webresources")
                .path("myresource")
                .path("checked")
                .path("mymappedthrowingexception")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the desired error page",
                responseMsg.contains("Checked Exception error page"));
    }

    @Test
    public void testNotFoundErrorPage1() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("404")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the desired NotFound.jsp error page",
                responseMsg.contains("Not Found Error Page"));
    }

    @Test
    public void testNotFoundErrorPage2() {
        WebResource webResource = resource();
        ClientResponse response = webResource
                .path("webresources")
                .path("404")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the desired NotFound.jsp error page",
                responseMsg.contains("Not Found Error Page"));
    }

    @Test
    public void testNotFoundErrorPage3() {
        WebResource webResource = resource();
        ClientResponse response = webResource
                .path("webapplicationexception")
                .path("404")
                .accept("text/plain").get(ClientResponse.class);
        String responseMsg = response.getEntity(String.class);
        assertTrue("Not redirected to the desired NotFound.jsp error page",
                responseMsg.contains("Not Found Error Page"));
    }

    @Test
    public void testMappableContainerExceptionPage() {

    }
    
}