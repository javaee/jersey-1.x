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
package com.sun.jersey.spi.monitoring;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractResourceModelContext;
import com.sun.jersey.api.model.AbstractResourceModelListener;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Jakub.Podlesak@Oracle.Com
 */
public class MonitoringProviderTest extends AbstractResourceTester {

    // TODO: unmapped errors

    enum MH /* MonitorHit */ {

        SubResource, SubResourceLocator, ResourceMethod, Request, Response, Error, MappedException
    };

    enum RP /* ResourcePattern */ {

        ResourceMethod, SubResourceMethod, SubResourceLocatorResourceMethod, MappedErrorResourceMethod, SubResourceLocatorMappedErrorResourceMethod, MappedErrorSubResourceMethod
    };

    static Map<RP, Set<MH>> PatternMap = new EnumMap<RP, Set<MH>>(RP.class) {

        {
            put(RP.ResourceMethod, EnumSet.of(MH.Request, MH.ResourceMethod, MH.Response));
            put(RP.SubResourceMethod, EnumSet.of(MH.Request, MH.ResourceMethod, MH.Response));
            put(RP.SubResourceLocatorResourceMethod, EnumSet.of(MH.Request, MH.SubResourceLocator, MH.SubResource, MH.ResourceMethod, MH.Response));
            put(RP.SubResourceLocatorMappedErrorResourceMethod, EnumSet.of(MH.Request, MH.SubResourceLocator, MH.SubResource, MH.ResourceMethod, MH.MappedException, MH.Response));
            put(RP.MappedErrorResourceMethod, EnumSet.of(MH.Request, MH.ResourceMethod, MH.MappedException, MH.Response));
            put(RP.MappedErrorSubResourceMethod, EnumSet.of(MH.Request, MH.ResourceMethod, MH.Response));
        }
    };

    @Provider
    public static class MyMonitor implements RequestListener, ResponseListener, DispatchingListener, AbstractResourceModelListener {

        Set<MH> requestPattern;
        AbstractResourceModelContext abstractModelContext;

        public void resetPattern() {
            requestPattern = EnumSet.noneOf(MH.class);
        }

        @Override
        public void onSubResource(long id, Class subResource) {
            requestPattern.add(MH.SubResource);
            System.out.println(String.format("####onSubResource: %d class: %s", id, subResource));
        }

        @Override
        public void onSubResourceLocator(long id, AbstractSubResourceLocator locator) {
            requestPattern.add(MH.SubResourceLocator);
            System.out.println(String.format("####onSubResourceLocator: %d locator: %s", id, locator));
        }

        @Override
        public void onResourceMethod(long id, AbstractResourceMethod method) {
            requestPattern.add(MH.ResourceMethod);
            System.out.println(String.format("####onResourceMethod: %d method: %s", id, method));
        }

        @Override
        public void onRequest(long id, ContainerRequest request) {
            requestPattern.add(MH.Request);
            System.out.println(String.format("####onRequest: %d request: %s", id, request));
        }

        @Override
        public void onError(long id, Throwable ex) {
            requestPattern.add(MH.Error);
            System.out.println(String.format("####onError: %d throwable: %s", id, ex));
        }

        @Override
        public void onResponse(long id, ContainerResponse response) {
            requestPattern.add(MH.Response);
            System.out.println(String.format("####onResponse: %d response: %s", id, response));
        }

        @Override
        public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
            requestPattern.add(MH.MappedException);
            System.out.println(String.format("####onMappedException: %d throwable: %s mapper: %s", id, exception, mapper));
        }

        @Override
        public void onLoaded(AbstractResourceModelContext modelContext) {
            abstractModelContext = modelContext;
            System.out.println(String.format("####onModelLoaded: %s", modelContext.getAbstractRootResources()));
        }
    }

    public static class MonitoredSubResource {

        @GET
        @Produces("plain/text")
        public String resourceMethod() {
            return "root/sub-resource-locator";
        }
    }

    public static class MonitoredErrorSubResource {

        @GET
        @Produces("plain/text")
        public String resourceMethod() throws MonitoringException {
            throw new MonitoringException();
        }
    }

    @Path("root")
    public static class MonitoredResource {

        @GET
        @Produces("plain/text")
        public String resourceMethod() {
            return "root";
        }

        @GET
        @Path("sub-resource-method")
        @Produces("plain/text")
        public String subResourceMethod() {
            return "root/sub-resource-method";
        }

        @Path("sub-resource-locator")
        public MonitoredSubResource subResourceLocator() {
            return new MonitoredSubResource();
        }

        @Path("error-sub-resource-locator")
        public MonitoredErrorSubResource errorSubResourceLocator() {
            return new MonitoredErrorSubResource();
        }
    }

    @Path("error-root")
    public static class MonitoredMappedErrorResource {

        @GET
        @Produces("plain/text")
        public String resourceMethod() throws MonitoringException {
            throw new MonitoringException();
        }

        @GET
        @Path("sub-resource-method")
        @Produces("plain/text")
        public String subResourceMethod() throws MonitoringException {
            throw new MonitoringException();
        }
    }

    public static class MonitoringException extends Exception {}

    public static class MonitoringExceptionMapper implements ExceptionMapper<MonitoringException> {

        @Override
        public Response toResponse(MonitoringException exception) {
            return Response.serverError().build();
        }

    }

    public MonitoringProviderTest(String testName) {
        super(testName);
    }

    public void testGet() throws Exception {

        ResourceConfig rc = new DefaultResourceConfig(MonitoredResource.class);
        final MyMonitor myMonitor = new MyMonitor();

        rc.getSingletons().add(myMonitor);
        rc.getSingletons().add(new MonitoringExceptionMapper());
        initiateWebApplication(rc);


        WebResource r = resource("/root", false);

        myMonitor.resetPattern();
        r.get(String.class);
        assertEquals(myMonitor.requestPattern, PatternMap.get(RP.ResourceMethod));

        myMonitor.resetPattern();
        r.path("sub-resource-method").get(String.class);
        assertEquals(myMonitor.requestPattern, PatternMap.get(RP.SubResourceMethod));
        
        myMonitor.resetPattern();
        r.path("sub-resource-locator").get(String.class);
        assertEquals(myMonitor.requestPattern, PatternMap.get(RP.SubResourceLocatorResourceMethod));

        myMonitor.resetPattern();
        r.path("error-sub-resource-locator").get(ClientResponse.class);
        assertEquals(myMonitor.requestPattern, PatternMap.get(RP.SubResourceLocatorMappedErrorResourceMethod));

        WebResource er = resource("/error-root", false);
        myMonitor.resetPattern();
        er.get(String.class);
        assertEquals(myMonitor.requestPattern, PatternMap.get(RP.MappedErrorResourceMethod));

        myMonitor.resetPattern();
        er.path("sub-resource-method").get(String.class);
        assertEquals(myMonitor.requestPattern, PatternMap.get(RP.MappedErrorSubResourceMethod));
    }
}
