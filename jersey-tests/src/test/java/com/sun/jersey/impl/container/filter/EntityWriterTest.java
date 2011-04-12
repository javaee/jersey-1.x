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

package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * @author pavel.bucek@oracle.com
 */
public class EntityWriterTest extends AbstractResourceTester {

    public EntityWriterTest(String testName) {
        super(testName);
    }

    @Path("a")
    public static class Resource {
        @POST
        @Path("1")
        public void post(String entity) {
            assertEquals(entity, "changed");
        }

        @POST
        @Path("2")
        public void post(XmlBean entity) {
            assertEquals(entity.s, "changed");
            assertEquals(entity.i, 2);
        }

    }

    @XmlRootElement
    public static class XmlBean {
        public String s;
        public int i;

        public XmlBean() {
        }

        public XmlBean(String s, int i) {
            this.s = s;
            this.i = i;
        }
    }

    public static class EntityModifyFilter implements ContainerRequestFilter {

        private final int type;

        EntityModifyFilter(int type) {
            this.type = type;
        }

        public ContainerRequest filter(ContainerRequest request) {
            switch(type) {
                case 1:
                    request.setEntity(String.class, String.class, new Annotation[0], MediaType.TEXT_PLAIN_TYPE,
                            new StringKeyIgnoreCaseMultivaluedMap<Object>(), "changed");
                    break;

                case 2:
                    request.setEntity(XmlBean.class, XmlBean.class, XmlBean.class.getAnnotations(), MediaType.APPLICATION_XML_TYPE,
                            new StringKeyIgnoreCaseMultivaluedMap<Object>(), new XmlBean("changed", 2));
            }

            return request;
        }
    }

    public void testString() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new EntityModifyFilter(1)));
        initiateWebApplication(rc);

        resource("/a/1").post("original");
    }

    public void testXml() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new EntityModifyFilter(2)));
        initiateWebApplication(rc);

        resource("/a/2").post(new XmlBean("original", 1));
    }


}
