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

package com.sun.jersey.samples.contacts.models;

import junit.framework.TestCase;
import org.apache.abdera.model.Entry;

/**
 * <p>Test case for {@link Contact} and its subordinate data models.</p>
 */
public class ContactTest extends TestCase {
    
    public ContactTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testComplexSerialization() {

        Contact contact1 = new Contact();
        contact1.getCategories().add("Category1");
        contact1.getCategories().add("Category2");
        contact1.getCategories().add("Category3");
        contact1.setContent("This is the first line of content.\r\nThis is the second line of content.\r\n");
        contact1.setName("Contact Name");
        contact1.getEmailAddresses().add(new EmailAddress("foo@example.com", null, true, Contact.REL_TYPE_HOME));
        contact1.getEmailAddresses().add(new EmailAddress("bar@example.com", null, false, Contact.REL_TYPE_WORK));
        contact1.getImAddresses().add(new ImAddress("jabber:foo@example.com", null, true, null, Contact.REL_TYPE_HOME));
        contact1.getImAddresses().add(new ImAddress("jabber:bar@example.com", null, false, null, Contact.REL_TYPE_WORK));
        contact1.getOrganizations().add(new Organization("Example Company", "Chief Cook and Bottle Washer", null, true, Contact.REL_TYPE_WORK));
        contact1.getPhoneNumbers().add(new PhoneNumber("555-555-1212", null, true, null, Contact.REL_TYPE_HOME));
        contact1.getPhoneNumbers().add(new PhoneNumber("777-555-1212", null, false, null, Contact.REL_TYPE_WORK));
        contact1.getPostalAddresses().add(new PostalAddress("555 Anywhere Street\r\nExample, CA", null, true, Contact.REL_TYPE_HOME));
        contact1.getPostalAddresses().add(new PostalAddress("777 Anywhere Place\r\nExample, CA", null, false, Contact.REL_TYPE_WORK));

        Entry entry1 = contact1.asEntry();
//        System.out.println(entry1.toString());
        Contact contact2 = Contact.fromEntry(entry1);
        assertTrue(contact1.equals(contact2));
        assertTrue(contact1.hashCode() == contact2.hashCode());

    }

    public void testSimpleSerialization() {
        Contact contact1 = new Contact();
        Entry entry1 = contact1.asEntry();
        Contact contact2 = Contact.fromEntry(entry1);
        assertTrue(contact1.equals(contact2));
        assertTrue(contact1.hashCode() == contact2.hashCode());
    }

}
