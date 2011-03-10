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
package com.sun.jersey.qe.tests.monitoring.wadlresource;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.qe.tests.monitoring.commons.ResourceMonitor;
import com.sun.jersey.server.impl.wadl.WadlResource;
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
public class WadlResourceMonitoringTest extends JerseyTest {

    private ResourceMonitor resourceMonitor;

    private String wadlResourceClassName = WadlResource.class.getName();

    public WadlResourceMonitoringTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.qe.tests.monitoring.wadlresource")
                .contextPath("wadl-resource-monitoring").build());
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
            resourceMonitor = new ResourceMonitor("wadl-resource-monitoring", AS_HOME);
        } else {
            resourceMonitor = new ResourceMonitor("wadl-resource-monitoring");
        }
    }

    @After
    public void afterClass() throws Exception {
        super.tearDown();
        resourceMonitor = null;
    }

    /**
     * Test that the wadl resource hit count in category "rootresourceclasshitcount"
     * is correct.
     * @throws java.lang.Exception
     */
    @Test
    public void testWadlRootResource() throws Exception {

        int wadlResourceInitialHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        /*
        wadlResourceInitialHitCount = resourceMonitor.getPerResourceHitCountValue(
                "rootresourceclasshitcount",
                "com.sun.jersey.server.impl.wadl.WadlResource"
                );
         */

        wadlResourceInitialHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                wadlResourceClassName);

        WebResource webResource = resource();
        String responseMsg = "";
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("root").get(String.class);
            assertEquals("Message from Root!", responseMsg);
        }

        /*
        int wadlResourceCurrentHitCount = resourceMonitor.getPerResourceHitCountValue(
                "rootresourceclasshitcount",
                "com.sun.jersey.server.impl.wadl.WadlResource"
                );
         */
        int wadlResourceCurrentHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                wadlResourceClassName);

        System.out.println("WADL RESOURCE INITIAL HIT COUNT (ROOT) ::" + wadlResourceInitialHitCount);
        System.out.println("WADL RESOURCE CURRENT HIT COUNT (ROOT) ::" + wadlResourceCurrentHitCount);

        assertEquals("Hit count of the WadlResource class doesn't match the expected value",
                wadlResourceInitialHitCount,
                wadlResourceCurrentHitCount);

        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("application.wadl").get(String.class);
        }

        wadlResourceCurrentHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                wadlResourceClassName);

        System.out.println("WADL RESOURCE INITIAL HIT COUNT (ROOT) ::" + wadlResourceInitialHitCount);
        System.out.println("WADL RESOURCE CURRENT HIT COUNT (ROOT) ::" + wadlResourceCurrentHitCount);

        assertEquals("Hit count of the WadlResource class doesn't match the expected value",
                wadlResourceInitialHitCount + numberOfRequestsToResource,
                wadlResourceCurrentHitCount);

    }

    /**
     * Test that the wadl resource hit count in category "resourceclasshitcount"
     * is correct.
     * @throws java.lang.Exception
     */
    @Test
    public void testWadlResource() throws Exception {

        int wadlResourceInitialHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        wadlResourceInitialHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                wadlResourceClassName);

        WebResource webResource = resource();
        String responseMsg = "";
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("root").get(String.class);
            assertEquals("Message from Root!", responseMsg);
        }

        int wadlResourceCurrentHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                wadlResourceClassName);

        System.out.println("WADL RESOURCE INITIAL HIT COUNT ::" + wadlResourceInitialHitCount);
        System.out.println("WADL RESOURCE CURRENT HIT COUNT ::" + wadlResourceCurrentHitCount);

        assertEquals("Hit count of the WadlResource class doesn't match the expected value",
                wadlResourceInitialHitCount,
                wadlResourceCurrentHitCount);

        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("application.wadl").get(String.class);
        }

        wadlResourceCurrentHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                wadlResourceClassName);

        System.out.println("WADL RESOURCE INITIAL HIT COUNT ::" + wadlResourceInitialHitCount);
        System.out.println("WADL RESOURCE CURRENT HIT COUNT ::" + wadlResourceCurrentHitCount);

        assertEquals("Hit count of the WadlResource class doesn't match the expected value",
                wadlResourceInitialHitCount + numberOfRequestsToResource,
                wadlResourceCurrentHitCount);

    }

    /**
     * Test that the wadl resource hit count in the two categories -
     * "rootresourceclasshitcount" and "resourceclasshitcount"  is the same.
     * @throws java.lang.Exception
     */
    @Test
    public void testWadlResourceHitCount() throws Exception {

        int wadlResourceRootHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        WebResource webResource = resource();
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            webResource.path("application.wadl").get(String.class);
        }

        wadlResourceRootHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                wadlResourceClassName);

        int wadlResourceHitCount = resourceMonitor
                .getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                wadlResourceClassName);

        System.out.println("WADL RESOURCE ROOT HIT COUNT ::" + wadlResourceRootHitCount);
        System.out.println("WADL RESOURCE HIT COUNT ::" + wadlResourceHitCount);

        assertEquals("Hit count of the WadlResource class in the two categories " +
                "\"rootresourceclasshitcount\" and \"resourceclasshitcount\" does " +
                "not match",
                wadlResourceRootHitCount,
                wadlResourceHitCount);

    }

}
