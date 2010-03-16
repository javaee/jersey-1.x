/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.server.linking;

import java.net.URI;
import java.util.Iterator;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;

/**
 *
 * @author mh124079
 */
public class EntityDescriptorTest extends TestCase {

    public EntityDescriptorTest(String testName) {
        super(testName);
    }

    public static class TestClassA {
        @Link
        protected String foo;

        @Link
        private String bar;

        public String baz;
    }

    /**
     * Test for declared properties
     */
    public void testDeclaredProperties() {
        System.out.println("Declared properties");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassA.class);
        assertEquals(2, instance.getLinkFields().size());
        assertEquals(1, instance.getNonLinkFields().size());
    }

    public static class TestClassB extends TestClassA {
        @Link
        private String bar;
    }

    /**
     * Test for inherited properties
     */
    public void testInheritedProperties() {
        System.out.println("Inherited properties");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassB.class);
        assertEquals(2, instance.getLinkFields().size());
        assertEquals(1, instance.getNonLinkFields().size());
    }

    private final static String TEMPLATE_A = "foo";

    @Path(TEMPLATE_A)
    public static class TestResourceA {
    }

    public static class TestClassC {
        @Link(resource=TestResourceA.class)
        String res;
    }

    public void testResourceLink() {
        System.out.println("Resource class link");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassC.class);
        assertEquals(1, instance.getLinkFields().size());
        assertEquals(0, instance.getNonLinkFields().size());
        LinkFieldDescriptor linkDesc = instance.getLinkFields().iterator().next();
        assertEquals(TEMPLATE_A, linkDesc.getLinkTemplate());
    }

    public static class TestClassD {
        @Link(value=TEMPLATE_A, style=Link.Style.RELATIVE_PATH)
        private String res1;

        @Link(value=TEMPLATE_A, style=Link.Style.RELATIVE_PATH)
        private URI res2;
    }

    public void testStringLink() {
        System.out.println("String link");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassD.class);
        assertEquals(2, instance.getLinkFields().size());
        assertEquals(0, instance.getNonLinkFields().size());
        Iterator<LinkFieldDescriptor> i = instance.getLinkFields().iterator();
        while (i.hasNext()) {
            LinkFieldDescriptor linkDesc = i.next();
            assertEquals(TEMPLATE_A, linkDesc.getLinkTemplate());
        }
    }

    public void testSetLink() {
        System.out.println("Set link");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassD.class);
        Iterator<LinkFieldDescriptor> i = instance.getLinkFields().iterator();
        TestClassD testClass = new TestClassD();
        while (i.hasNext()) {
            LinkFieldDescriptor linkDesc = i.next();
            URI value = UriBuilder.fromPath(linkDesc.getLinkTemplate()).build();
            linkDesc.setPropertyValue(testClass, value);
        }
        assertEquals(TEMPLATE_A, testClass.res1);
        assertEquals(TEMPLATE_A, testClass.res2.toString());
    }

}
