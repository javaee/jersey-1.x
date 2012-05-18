/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.impl.xml;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.provider.jaxb.XmlHeader;
import com.sun.jersey.impl.AbstractResourceTester;

/**
 * @author Martin Matula (martin.matula at oracle.com)
 */
public class XmlHeaderTest extends AbstractResourceTester {

    private static final String XML_STYLESHEET = "<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>";

    public XmlHeaderTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class FooResource {

        @Path("root")
        @GET
        @Produces({"application/xml", "application/json"})
        @XmlHeader(XML_STYLESHEET)
        public RootElement getRoot() {
            RootElement re = new RootElement();
            re.name = "jmeno";
            re.value = "hodnota";
            return re;
        }

        @Path("element")
        @GET
        @Produces({"application/xml", "application/json"})
        @XmlHeader(XML_STYLESHEET)
        public JAXBElement<Element> getElement() {
            Element re = new Element();
            re.name = "jmeno";
            return new JAXBElement<Element>(QName.valueOf("blabla.blabla"), Element.class, re);
        }

        @Path("list")
        @GET
        @Produces({"application/xml", "application/json"})
        @XmlHeader(XML_STYLESHEET)
        public List<RootElement> getList() {
            RootElement re = new RootElement();
            re.name = "jmeno";
            re.value = "hodnota";
            return Arrays.asList(new RootElement[] {re});
        }

        @Path("root-nh")
        @GET
        @Produces({"application/xml", "application/json"})
        public RootElement getRootNH() {
            RootElement re = new RootElement();
            re.name = "jmeno";
            re.value = "hodnota";
            return re;
        }

        @Path("element-nh")
        @GET
        @Produces({"application/xml", "application/json"})
        public JAXBElement<Element> getElementNH() {
            Element re = new Element();
            re.name = "jmeno";
            return new JAXBElement<Element>(QName.valueOf("blabla.blabla"), Element.class, re);
        }

        @Path("list-nh")
        @GET
        @Produces({"application/xml", "application/json"})
        public List<RootElement> getListNH() {
            RootElement re = new RootElement();
            re.name = "jmeno";
            re.value = "hodnota";
            return Arrays.asList(new RootElement[] {re});
        }
    }

    public void testRoot() throws Exception {
        tryResource("/root");
    }

    public void testElement() throws Exception {
        tryResource("/element");
    }

    public void testList() throws Exception {
        tryResource("/list");
    }

    private void tryResource(String resource) throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(FooResource.class);
        initiateWebApplication(rc);

        String s = resource(resource).type(MediaType.APPLICATION_XML).get(String.class);
        assertTrue("Wrong message: " + s, s.contains(XML_STYLESHEET));
        s = resource(resource + "-nh").type(MediaType.APPLICATION_XML).get(String.class);
        assertTrue("Wrong message: " + s, s.contains("jmeno") && !s.contains("<?xml-stylesheet"));
        s = resource(resource).type(MediaType.APPLICATION_XML).get(String.class);
        assertTrue("Wrong message: " + s, s.contains(XML_STYLESHEET));
    }

}