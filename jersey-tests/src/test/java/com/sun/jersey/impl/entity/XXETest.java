/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.impl.AbstractResourceTester;
import java.net.URL;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class XXETest extends AbstractResourceTester {
    private static final String DOCTYPE =
            "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"%s\">]>";
    
    private static final String XML =
            "<jaxbBean><value>&xxe;</value></jaxbBean>";

    public XXETest(String testName) {
        super(testName);
    }
    
    private String getDocument() {
        URL u = this.getClass().getResource("xxe.txt");
        return String.format(DOCTYPE, u.toString()) + XML;
    }

    private String getListDocument() {
        URL u = this.getClass().getResource("xxe.txt");
        return String.format(DOCTYPE, u.toString()) +
                "<jAXBBeans>" +
                XML + XML + XML +
                "</jAXBBeans>";
    }

    @Path("/")
    @Consumes("application/xml")
    @Produces("application/xml")
    public static class EntityHolderResource {
        @Path("jaxb")
        @POST
        public String post(JAXBBean s) {
            return s.value;
        }

        @Path("jaxb/list")
        @POST
        public String post(List<JAXBBean> s) {
            return s.get(0).value;
        }

        @Path("sax")
        @POST
        public SAXSource postSax(SAXSource s) {
           return s;
        }

        @Path("dom")
        @POST
        public DOMSource postDom(DOMSource s) {
           return s;
        }
    }

    public void testJAXBSecure() {
        initiateWebApplication(EntityHolderResource.class);

        WebResource r = resource("/");

        String s = r.path("jaxb").type("application/xml").post(String.class, getDocument());
        assertEquals("", s);
    }

    public void testJAXBInsecure() {
        ClassNamesResourceConfig rc = new ClassNamesResourceConfig(EntityHolderResource.class);
        rc.getFeatures().put(FeaturesAndProperties.FEATURE_DISABLE_XML_SECURITY, Boolean.TRUE);
        initiateWebApplication(rc);

        WebResource r = resource("/");

        String s = r.path("jaxb").type("application/xml").post(String.class, getDocument());
        assertEquals("COMPROMISED", s);
    }

    public void testJAXBListSecure() {
        initiateWebApplication(EntityHolderResource.class);

        WebResource r = resource("/");

        String s = r.path("jaxb/list").type("application/xml").post(String.class, getListDocument());
        assertEquals("", s);
    }

    public void testJAXBListInsecure() {
        ClassNamesResourceConfig rc = new ClassNamesResourceConfig(EntityHolderResource.class);
        rc.getFeatures().put(FeaturesAndProperties.FEATURE_DISABLE_XML_SECURITY, Boolean.TRUE);
        initiateWebApplication(rc);

        WebResource r = resource("/");

        String s = r.path("jaxb/list").type("application/xml").post(String.class, getListDocument());
        assertEquals("COMPROMISED", s);
    }

    public void testSAXSecure() {
        initiateWebApplication(EntityHolderResource.class);

        WebResource r = resource("/");

        JAXBBean b = r.path("sax").type("application/xml").post(JAXBBean.class, getDocument());
        assertEquals("", b.value);
    }

    public void testSAXInsecure() {
        ClassNamesResourceConfig rc = new ClassNamesResourceConfig(EntityHolderResource.class);
        rc.getFeatures().put(FeaturesAndProperties.FEATURE_DISABLE_XML_SECURITY, Boolean.TRUE);
        initiateWebApplication(rc);

        WebResource r = resource("/");

        JAXBBean b = r.path("sax").type("application/xml").post(JAXBBean.class, getDocument());
        assertEquals("COMPROMISED", b.value);
    }

    public void testDOMSecure() {
        initiateWebApplication(EntityHolderResource.class);

        WebResource r = resource("/");

        JAXBBean b = r.path("dom").type("application/xml").post(JAXBBean.class, getDocument());
        assertEquals("", b.value);
    }

    public void testDOMInsecure() {
        ClassNamesResourceConfig rc = new ClassNamesResourceConfig(EntityHolderResource.class);
        rc.getFeatures().put(FeaturesAndProperties.FEATURE_DISABLE_XML_SECURITY, Boolean.TRUE);
        initiateWebApplication(rc);

        WebResource r = resource("/");

        JAXBBean b = r.path("dom").type("application/xml").post(JAXBBean.class, getDocument());
        assertEquals("COMPROMISED", b.value);
    }
}