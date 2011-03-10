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
package com.sun.jersey.samples.helloworld;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.qe.tests.monitoring.commons.ResourceMonitor;
import com.sun.jersey.samples.helloworld.resources.HelloWorldResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class HelloWorldWebAppTest extends JerseyTest {

    private ResourceMonitor resourceMonitor;

    private String resourceClassName = HelloWorldResource.class.getName();

    public HelloWorldWebAppTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.samples.helloworld.resources")
                .contextPath("helloworld-webapp").build());
        Client client = client();

    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new ExternalTestContainerFactory();
    }

    @Before
    public void beforeClass() throws Exception {
        super.setUp();
        String AS_HOME = System.getProperty("as.home");
        if (AS_HOME != null && !AS_HOME.equals("")) {
            resourceMonitor = new ResourceMonitor("helloworld-webapp", AS_HOME);
        } else {
            resourceMonitor = new ResourceMonitor("helloworld-webapp");
        }
    }

    @After
    public void afterClass() throws Exception {
        super.tearDown();
        resourceMonitor = null;
    }

    /**
     * Test that the expected response is sent back.
     * @throws java.lang.Exception
     */
     @Test
    public void testHelloWorld() throws Exception {

        int helloResourceInitialHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        /*
        helloResourceInitialHitCount = resourceMonitor.getPerResourceHitCountValue("resourceclasshitcount",
                "com.sun.jersey.samples.helloworld.resources.HelloWorldResource");
         */

        helloResourceInitialHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count", resourceClassName);
       
        WebResource webResource = resource();
        String responseMsg = "";
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("helloworld").get(String.class);
            assertEquals("Hello World", responseMsg);
        }

        /*
        int helloResourceCurrentHitCount = resourceMonitor.getPerResourceHitCountValue("resourceclasshitcount",
                "com.sun.jersey.samples.helloworld.resources.HelloWorldResource");
         */

        int helloResourceCurrentHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count", resourceClassName);

        System.out.println("INITIAL HIT COUNT ::" + helloResourceInitialHitCount);
        System.out.println("CURRENT HIT COUNT ::" + helloResourceCurrentHitCount);

        assertEquals("Hit count of the HelloResource class doesn't match the expected value",
                helloResourceInitialHitCount + numberOfRequestsToResource,
                helloResourceCurrentHitCount);
         
    }

    @Test
    public void testApplicationWadl() {
        WebResource webResource = resource();
        String serviceWadl = webResource.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);

        assertTrue(serviceWadl.length() > 0);
    }

    private int getAdminPort(int adminPort) {
        return ((System.getProperty("JERSEY_ADMIN_PORT") != null)
                ? Integer.parseInt(System.getProperty("JERSEY_ADMIN_PORT"))
                : adminPort);
    }


   @Test
    public void testAttrFrmAdminCLI() throws Exception {

        assertEquals("resource-description mismatch"
                , "Resource class hit count for " + resourceClassName
                ,resourceMonitor.getResourceMonitoredValueFromAdminCLI("resourcehitcount-description"
                , resourceClassName));

        assertEquals("rootresource-description mismatch"
                , "Root resource class hit count for " + resourceClassName
                , resourceMonitor.getResourceMonitoredValueFromAdminCLI("rootresourcehitcount-description"
                , resourceClassName));

    }

}
