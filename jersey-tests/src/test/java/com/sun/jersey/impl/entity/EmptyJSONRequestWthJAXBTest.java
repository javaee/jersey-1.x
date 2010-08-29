/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyJSONRequestWthJAXBTest extends AbstractResourceTester {
    
    public EmptyJSONRequestWthJAXBTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @POST
        public void bean(JAXBBean b) {
        }

        @Path("type")
        @POST
        public void type(JAXBBeanType b) {
        }

        @Path("list-bean")
        @POST
        public void listBean(List<JAXBBean> b) {
        }

        @Path("list-type")
        @POST
        public void listType(List<JAXBBeanType> b) {
        }

        @Path("array-bean")
        @POST
        public void arrayBean(JAXBBean[] b) {
        }

        @Path("array-type")
        @POST
        public void arrayType(JAXBBeanType[] b) {
        }

    }
    
    public void testEmptyRequestMapped() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/", false);

        _test(r);
    }

    public static abstract class CR implements ContextResolver<JAXBContext> {

        private final JAXBContext context;

        private final Class[] classes = {JAXBBean.class, JAXBBeanType.class};

        private final Set<Class> types = new HashSet(Arrays.asList(classes));

        public CR() {
            try {
                context = configure(classes);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }

        protected abstract JAXBContext configure(Class[] classes) throws JAXBException;

        public JAXBContext getContext(Class<?> objectType) {
            return (types.contains(objectType)) ? context : null;
        }
    }


    public static class NaturalCR extends CR {
        protected JAXBContext configure(Class[] classes) throws JAXBException {
            return new JSONJAXBContext(JSONConfiguration.natural().build(), classes);
        }
    }

    public void testEmptyRequestNatural() {
        initiateWebApplication(NaturalCR.class, Resource.class);
        WebResource r = resource("/", false);
        r.addFilter(new LoggingFilter());

        _test(r);
    }


    public static class MappedJettisonCR extends CR {
        protected JAXBContext configure(Class[] classes) throws JAXBException {
            return new JSONJAXBContext(JSONConfiguration.mappedJettison().build(), classes);
        }
    }

    public void testMappedJettisonCR() {
        initiateWebApplication(MappedJettisonCR.class, Resource.class);
        WebResource r = resource("/", false);

        _test(r);
    }

    
    public static class BadgerFishCR extends CR {
        protected JAXBContext configure(Class[] classes) throws JAXBException {
            return new JSONJAXBContext(JSONConfiguration.badgerFish().build(), classes);
        }
    }

    public void testBadgerFishCR() {
        initiateWebApplication(BadgerFishCR.class, Resource.class);
        WebResource r = resource("/", false);

        _test(r);
    }

    
    public void _test(WebResource r) {
        ClientResponse cr = r.type("application/json").post(ClientResponse.class);
        assertEquals(400, cr.getStatus());

        r.path("type").type("application/json").post(ClientResponse.class);
        assertEquals(400, cr.getStatus());

        r.path("list-bean").type("application/json").post(ClientResponse.class);
        assertEquals(400, cr.getStatus());

        r.path("list-type").type("application/json").post(ClientResponse.class);
        assertEquals(400, cr.getStatus());

        r.path("array-bean").type("application/json").post(ClientResponse.class);
        assertEquals(400, cr.getStatus());

        r.path("array-type").type("application/json").post(ClientResponse.class);
        assertEquals(400, cr.getStatus());
    }
}