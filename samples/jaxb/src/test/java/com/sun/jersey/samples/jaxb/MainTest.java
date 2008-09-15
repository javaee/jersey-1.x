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

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import java.util.Collection;
import javax.ws.rs.core.GenericEntity;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MainTest extends TestCase {
    
    SelectorThread threadSelector;
    
    WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        threadSelector = Main.startServer();

        Client c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (threadSelector != null) {
            threadSelector.stopEndpoint();
        }
    }

    public void testRootElement() {
        String s = r.path("jaxb/XmlRootElement").type("application/xml").
                get(String.class);

        JAXBXmlRootElement e1 = r.path("jaxb/XmlRootElement").
                get(JAXBXmlRootElement.class);
        
        JAXBXmlRootElement e2 = r.path("jaxb/XmlRootElement").type("application/xml").
                post(JAXBXmlRootElement.class, e1);

        assertEquals(e1, e2);
    }
    
    public void testJAXBElement() {
        GenericType<JAXBElement<JAXBXmlType>> genericType = 
                new GenericType<JAXBElement<JAXBXmlType>>() {};
                
        JAXBElement<JAXBXmlType> e1 = r.path("jaxb/JAXBElement").
                get(genericType);
        
        JAXBElement<JAXBXmlType> e2 = r.path("jaxb/JAXBElement").type("application/xml").
                post(genericType, e1);

        assertEquals(e1.getValue(), e2.getValue());
    }

    public void testXmlType() {
        JAXBXmlType t1 = r.path("jaxb/JAXBElement").
                get(JAXBXmlType.class);
        
        JAXBElement<JAXBXmlType> e = new JAXBElement<JAXBXmlType>(
                new QName("jaxbXmlRootElement"),
                JAXBXmlType.class,
                t1);
        JAXBXmlType t2 = r.path("jaxb/XmlType").type("application/xml").
                post(JAXBXmlType.class, e);
        
        assertEquals(t1, t2);
    }
    
    public void testRootElementCollection() {
        GenericType<Collection<JAXBXmlRootElement>> genericType = 
                new GenericType<Collection<JAXBXmlRootElement>>() {};
        
        Collection<JAXBXmlRootElement> ce1 = r.path("jaxb/collection/XmlRootElement").
                get(genericType);
        Collection<JAXBXmlRootElement> ce2 = r.path("jaxb/collection/XmlRootElement").
                type("application/xml").
                post(genericType, new GenericEntity<Collection<JAXBXmlRootElement>>(ce1){});
                
        assertEquals(ce1, ce2);
    }

    public void testXmlTypeCollection() {
        GenericType<Collection<JAXBXmlRootElement>> genericRootElement =
                new GenericType<Collection<JAXBXmlRootElement>>() {};
        GenericType<Collection<JAXBXmlType>> genericXmlType = 
                new GenericType<Collection<JAXBXmlType>>() {};
                
        Collection<JAXBXmlRootElement> ce1 = r.path("jaxb/collection/XmlRootElement").
                get(genericRootElement);
        
        Collection<JAXBXmlType> ct1 = r.path("jaxb/collection/XmlType").
                type("application/xml").
                post(genericXmlType, new GenericEntity<Collection<JAXBXmlRootElement>>(ce1){});
                
        Collection<JAXBXmlType> ct2 = r.path("jaxb/collection/XmlRootElement").
                get(genericXmlType);
                
        assertEquals(ct1, ct2);
    }
}
