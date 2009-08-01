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
package com.sun.jersey.samples.jaxb;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import java.util.Collection;
import javax.ws.rs.core.GenericEntity;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MainTest extends JerseyTest {

    public MainTest() throws Exception {
        super("com.sun.jersey.samples.jaxb");
    }
    
    /**
     * Test checks that the application.wadl is reachable.
     */
    @Test
    public void testApplicationWadl() {
        WebResource webResource = resource();
        String applicationWadl = webResource.path("application.wadl").get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0",
                applicationWadl.length() > 0);
    }

    @Test
    public void testRootElement() {
        WebResource webResource = resource();
        JAXBXmlRootElement e1 = webResource.path("jaxb/XmlRootElement").
                get(JAXBXmlRootElement.class);
        
        JAXBXmlRootElement e2 = webResource.path("jaxb/XmlRootElement").type("application/xml").
                post(JAXBXmlRootElement.class, e1);

        assertEquals(e1, e2);
    }

    @Test
    public void testJAXBElement() {
        WebResource webResource = resource();
        GenericType<JAXBElement<JAXBXmlType>> genericType = 
                new GenericType<JAXBElement<JAXBXmlType>>() {};
                
        JAXBElement<JAXBXmlType> e1 = webResource.path("jaxb/JAXBElement").
                get(genericType);
        
        JAXBElement<JAXBXmlType> e2 = webResource.path("jaxb/JAXBElement").type("application/xml").
                post(genericType, e1);

        assertEquals(e1.getValue(), e2.getValue());
    }

    @Test
    public void testXmlType() {
        WebResource webResource = resource();
        JAXBXmlType t1 = webResource.path("jaxb/JAXBElement").
                get(JAXBXmlType.class);
        
        JAXBElement<JAXBXmlType> e = new JAXBElement<JAXBXmlType>(
                new QName("jaxbXmlRootElement"),
                JAXBXmlType.class,
                t1);
        JAXBXmlType t2 = webResource.path("jaxb/XmlType").type("application/xml").
                post(JAXBXmlType.class, e);
        
        assertEquals(t1, t2);
    }

    @Test
    public void testRootElementCollection() {
        WebResource webResource = resource();
        GenericType<Collection<JAXBXmlRootElement>> genericType = 
                new GenericType<Collection<JAXBXmlRootElement>>() {};
        
        Collection<JAXBXmlRootElement> ce1 = webResource.path("jaxb/collection/XmlRootElement").
                get(genericType);
        Collection<JAXBXmlRootElement> ce2 = webResource.path("jaxb/collection/XmlRootElement").
                type("application/xml").
                post(genericType, new GenericEntity<Collection<JAXBXmlRootElement>>(ce1){});
                
        assertEquals(ce1, ce2);
    }

    @Test
    public void testXmlTypeCollection() {
        WebResource webResource = resource();
        GenericType<Collection<JAXBXmlRootElement>> genericRootElement =
                new GenericType<Collection<JAXBXmlRootElement>>() {};
        GenericType<Collection<JAXBXmlType>> genericXmlType = 
                new GenericType<Collection<JAXBXmlType>>() {};
                
        Collection<JAXBXmlRootElement> ce1 = webResource.path("jaxb/collection/XmlRootElement").
                get(genericRootElement);
        
        Collection<JAXBXmlType> ct1 = webResource.path("jaxb/collection/XmlType").
                type("application/xml").
                post(genericXmlType, new GenericEntity<Collection<JAXBXmlRootElement>>(ce1){});
                
        Collection<JAXBXmlType> ct2 = webResource.path("jaxb/collection/XmlRootElement").
                get(genericXmlType);
                
        assertEquals(ct1, ct2);
    }

    @Test
    public void testRootElementArray() {
        WebResource webResource = resource();
        JAXBXmlRootElement[] ae1 = webResource.path("jaxb/array/XmlRootElement").
                get(JAXBXmlRootElement[].class);
        JAXBXmlRootElement[] ae2 = webResource.path("jaxb/array/XmlRootElement").
                type("application/xml").
                post(JAXBXmlRootElement[].class, ae1);

        assertEquals(ae1.length, ae2.length);
        for (int i = 0; i < ae1.length; i++)
            assertEquals(ae1[i], ae2[i]);
    }

    @Test
    public void testXmlTypeArray() {
        WebResource webResource = resource();
        JAXBXmlRootElement[] ae1 = webResource.path("jaxb/array/XmlRootElement").
                get(JAXBXmlRootElement[].class);

        JAXBXmlType[] at1 = webResource.path("jaxb/array/XmlType").
                type("application/xml").
                post(JAXBXmlType[].class, ae1);

        JAXBXmlType[] at2 = webResource.path("jaxb/array/XmlRootElement").
                get(JAXBXmlType[].class);

        assertEquals(at1.length, at2.length);
        for (int i = 0; i < at1.length; i++)
            assertEquals(at1[i], at2[i]);
    }

}
