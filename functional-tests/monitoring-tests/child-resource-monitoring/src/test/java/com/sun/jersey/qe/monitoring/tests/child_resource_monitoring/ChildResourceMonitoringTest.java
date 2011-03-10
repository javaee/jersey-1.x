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
package com.sun.jersey.qe.monitoring.tests.child_resource_monitoring;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.MainResource;
import com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.more.SubResource;
import com.sun.jersey.qe.tests.monitoring.commons.ResourceMonitor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author naresh
 */
public class ChildResourceMonitoringTest extends JerseyTest {

    private ResourceMonitor resourceMonitor;

    private String mainResourceClassName = MainResource.class.getName();

    private String subResourceClassName = SubResource.class.getName();

    public ChildResourceMonitoringTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources")
                .contextPath("child-resources").build());        
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
            resourceMonitor = new ResourceMonitor("child-resource-monitoring", AS_HOME);
        } else {
            resourceMonitor = new ResourceMonitor("child-resource-monitoring");
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
    public void testRootResource() throws Exception {

        int rootResourceInitialHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        /*
        rootResourceInitialHitCount = resourceMonitor.getPerResourceHitCountValue("rootresourceclasshitcount",
                "com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.MainResource");
         */
        rootResourceInitialHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                mainResourceClassName);

        WebResource webResource = resource();
        String responseMsg = "";
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("root").get(String.class);
            assertEquals("Message from the root resource", responseMsg);            
        }

        /*
        int rootResourceCurrentHitCount = resourceMonitor.getPerResourceHitCountValue("rootresourceclasshitcount",
                "com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.MainResource");
         */

        int rootResourceCurrentHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                mainResourceClassName);

        System.out.println("INITIAL HIT COUNT ::" + rootResourceInitialHitCount);
        System.out.println("CURRENT HIT COUNT ::" + rootResourceCurrentHitCount);

        assertEquals("Hit count of the MainResource class doesn't match the expected value",
                rootResourceInitialHitCount + numberOfRequestsToResource,
                rootResourceCurrentHitCount);

        assertEquals("resource-description mismatch"
                , "Resource class hit count for " + mainResourceClassName
                ,resourceMonitor.getResourceMonitoredValueFromAdminCLI("resourcehitcount-description",
                mainResourceClassName));
        assertEquals("rootresource-description mismatch"
                , "Root resource class hit count for " + mainResourceClassName
                , resourceMonitor.getResourceMonitoredValueFromAdminCLI("rootresourcehitcount-description",
                mainResourceClassName));

    }

    @Test
    public void testSubResource() throws Exception {

        int rootResourceInitialHitCount = 0;
        int subResourceInitialHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        /*
        rootResourceInitialHitCount = resourceMonitor.getPerResourceHitCountValue("rootresourceclasshitcount",
                "com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.MainResource");
         */
        rootResourceInitialHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                mainResourceClassName);
        /*
        subResourceInitialHitCount = resourceMonitor.getPerResourceHitCountValue("resourceclasshitcount",
                "com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.more.SubResource");
         */
        subResourceInitialHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                subResourceClassName);

        WebResource webResource = resource();
        String responseMsg = "";
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("root").path("sub").get(String.class);
            assertEquals("Message from the sub resource class SubResource", responseMsg);
        }

        /*
        int rootResourceCurrentHitCount = resourceMonitor.getPerResourceHitCountValue("rootresourceclasshitcount",
                "com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.MainResource");
         */
        int rootResourceCurrentHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                mainResourceClassName);

        /*
        int subResourceCurrentHitCount = resourceMonitor.getPerResourceHitCountValue("resourceclasshitcount",
                "com.sun.jersey.qe.monitoring.tests.child_resource_monitoring.resources.more.SubResource");
         */
        int subResourceCurrentHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                subResourceClassName);

        System.out.println("ROOT RESOURCE INITIAL HIT COUNT ::" + rootResourceInitialHitCount);
        System.out.println("ROOT RESOURCE CURRENT HIT COUNT ::" + rootResourceCurrentHitCount);
        System.out.println("SUB RESOURCE INITIAL HIT COUNT ::" + subResourceInitialHitCount);
        System.out.println("SUB RESOURCE CURRENT HIT COUNT ::" + subResourceCurrentHitCount);

        assertEquals("Hit count of the root resource MainResource doesn't match the expected value",
                rootResourceInitialHitCount + numberOfRequestsToResource,
                rootResourceCurrentHitCount);

        assertEquals("Hit count of the sub resource SubResource doesn't match the expected value",
                subResourceInitialHitCount + numberOfRequestsToResource,
                subResourceCurrentHitCount);

    }

    @Test
    public void testMoreFromRoot() {
        int rootResourceInitialHitCount = 0;
        int resourceInitialHitCount = 0;
        String requiredNumberOfResourceHits = System.getProperty("resource_hits");
        int numberOfRequestsToResource = (requiredNumberOfResourceHits != null)
                ? Integer.parseInt(requiredNumberOfResourceHits)
                : 1;

        rootResourceInitialHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                mainResourceClassName);

        resourceInitialHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                mainResourceClassName);

        WebResource webResource = resource();
        String responseMsg = "";
        for(int requestIndex = 0; requestIndex < numberOfRequestsToResource; requestIndex++) {
            responseMsg = webResource.path("root").path("sub2").get(String.class);
            assertEquals("Message from method of the root resource class", responseMsg);
        }

        int rootResourceCurrentHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("rootresourcehitcount-count",
                mainResourceClassName);

        int resourceCurrentHitCount = resourceMonitor.getResourceMonitoredIntValueFromAdminCLI("resourcehitcount-count",
                mainResourceClassName);

        System.out.println("ROOT RESOURCE INITIAL HIT COUNT ::" + rootResourceInitialHitCount);
        System.out.println("ROOT RESOURCE CURRENT HIT COUNT ::" + rootResourceCurrentHitCount);
        System.out.println("RESOURCE INITIAL HIT COUNT ::" + resourceInitialHitCount);
        System.out.println("RESOURCE CURRENT HIT COUNT ::" + resourceCurrentHitCount);

        assertEquals("Hit count of the root resource MainResource, in category " +
                "\"rootresourceclasshitcount\" doesn't match the expected value",
                rootResourceInitialHitCount + numberOfRequestsToResource,
                rootResourceCurrentHitCount);

        assertEquals("Hit count of the root resource MainResource, in category " +
                "\"resourceclasshitcount\" doesn't match the expected value",
                resourceInitialHitCount + numberOfRequestsToResource,
                resourceCurrentHitCount);
    }
    
}
