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

package com.sun.jersey.samples.contacts.client;

import com.sun.jersey.samples.contacts.models.Contact;
import java.util.List;

/**
 * <p>Unit tests for client access to the Contacts Service.</p>
 */
public class ContactsClientTest extends AbstractTest {

    public ContactsClientTest(String testName) {
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

    public void testCreateUpdateDeleteContact() {
        Contact contact = new Contact();
        contact.setId("new_id");
        contact.setName("New Name");
        contact.setContent("New Content");
        client.createContact("admin", contact);
        contact = client.findContact("admin", "new_id");
        assertNotNull(contact);
        assertEquals("new_id", contact.getId());
        assertEquals("New Name", contact.getName());
        assertEquals("New Content", contact.getContent());
        List<Contact> contacts = client.findContacts("admin");
        assertEquals(1, contacts.size());
        contact = contacts.get(0);
        assertEquals("new_id", contact.getId());
        assertEquals("New Name", contact.getName());
        assertEquals("New Content", contact.getContent());
        contact.setName("Updated Name");
        contact.setContent("Updated Content");
        client.updateContact("admin", contact);
        contact = client.findContact("admin", "new_id");
        assertEquals("new_id", contact.getId());
        assertEquals("Updated Name", contact.getName());
        assertEquals("Updated Content", contact.getContent());
        contacts = client.findContacts("admin");
        assertEquals(1, contacts.size());
        contact = contacts.get(0);
        assertEquals("new_id", contact.getId());
        assertEquals("Updated Name", contact.getName());
        assertEquals("Updated Content", contact.getContent());
        client.deleteContact("admin", "new_id");
        contacts = client.findContacts("admin");
        assertEquals(0, contacts.size());
        try {
            client.findContact("admin", "new_id");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
    }

    public void testAuthenticationNegative() {
        try {
            client = new ContactsClient("http://localhost:" + getPort(9998), "badusername", "badpassword");
            client.findContact("admin", "an_id");
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected result
        }
    }

    public void testFindContactNegative() {
        try {
            client.findContact("admin", "badid");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
    }


    public void testFindContactsNegative() {
        try {
            client.findContacts("badusername");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            //
        }
    }


    public void testFindContactsPositive() {
        List<Contact> contacts = client.findContacts("admin");
        assertEquals(0, contacts.size());
    }


    /*
    public void testFindContactPositive() {
        String credentials = adminCredentials();
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            try {
                addEntry(credentials, mediaType, "admin", "new_id");
                Entry entry = getEntry(credentials, mediaType, "admin", "new_id");
                deleteEntry(credentials, mediaType, "admin", "new_id");
            } catch (UniformInterfaceException e) {
                e.printStackTrace(System.out);
                fail("Exception getting feed for media type " + mediaType + ": " + e);
            }
        }
    }

    public void testGetFeed() {
        String credentials = adminCredentials();
        for (String mediaType : FEED_MEDIA_TYPES) {
            try {
                Feed feed = getFeed(credentials, mediaType, "admin");
                assertNotNull("Got feed for media type " + mediaType, feed);
                assertEquals("Feed id for media type " + mediaType, "contacts", feed.getId().toString());
                assertEquals("Feed title for media type " + mediaType, "Contacts System Users for 'admin'", feed.getTitle());
                assertEquals("Feed self link for media type " + mediaType, 1, feed.getLinks("self").size());
                assertEquals("Feed entries for media type " + mediaType, 0, feed.getEntries().size());
                assertNotNull("Feed updated for media type " + mediaType, feed.getUpdated());
            } catch (UniformInterfaceException e) {
                e.printStackTrace(System.out);
                fail("Exception getting feed for media type " + mediaType + ": " + e);
            }
        }
    }

    public void testPostFeedPositive() {
        String credentials = adminCredentials();
        Feed feed = null;
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            feed = getFeed(credentials, "application/atom+xml;type=feed", "admin");
            assertEquals("Before entries for media type " + mediaType, 0, feed.getEntries().size());
            addEntry(credentials, mediaType, "admin", "new_id");
            Entry entry = getEntry(credentials, mediaType, "admin", "new_id");
            assertEquals("New content for media type " + mediaType, "new content", entry.getContent());
            assertEquals("New id for media type " + mediaType, "new_id", entry.getId().toString());
            assertEquals("New title for media type " + mediaType, "new name", entry.getTitle());
            feed = getFeed(credentials, "application/atom+xml;type=feed", "admin");
            assertEquals("After entries for media type " + mediaType, 1, feed.getEntries().size());
            deleteEntry(credentials, mediaType, "admin", feed.getEntries().get(0).getId().toString());
            feed = getFeed(credentials, "application/atom+xml;type=feed", "admin");
            assertEquals("Cleaned entries for media type " + mediaType, 0, feed.getEntries().size());
        }
    }

    public void testPutEntryPositive() {
        String credentials = adminCredentials();
        Contact contact = null;
        Entry entry = null;
        Feed feed = null;
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            addEntry(credentials, mediaType, "admin", "new_id");
            entry = getEntry(credentials, mediaType, "admin", "new_id");
            contact = Contact.fromEntry(entry);
            assertEquals("New content for media type " + mediaType, "new content", contact.getContent());
            assertEquals("New id for media type " + mediaType, "new_id", contact.getId());
            assertEquals("New title for media type " + mediaType, "new name", contact.getName());
            feed = getFeed(credentials, "application/atom+xml;type=feed", "admin");
            assertEquals("After entries for media type " + mediaType, 1, feed.getEntries().size());
            contact.setName("updated name");
            putEntry(credentials, mediaType, "admin", contact);
            entry = getEntry(credentials, mediaType, "admin", "new_id");
            contact = Contact.fromEntry(entry);
            assertEquals("Updated name for media type " + mediaType, "updated name", contact.getName());
            deleteEntry(credentials, mediaType, "admin", feed.getEntries().get(0).getId().toString());
            feed = getFeed(credentials, "application/atom+xml;type=feed", "admin");
            assertEquals("Cleaned entries for media type " + mediaType, 0, feed.getEntries().size());
        }
    }

    private void addEntry(String credentials, String mediaType, String username, String id) {
        Contact contact = new Contact();
        contact.setContent("new content");
        contact.setId(id);
        contact.setName("new name");
        contact.setUpdated(new Date());
        postEntry(credentials, mediaType, username, contact);
    }

    private void deleteEntry(String credentials, String mediaType, String username, String id) {
        service.
          path("contacts").
          path(username).
          path(id).
          header("Authorization", credentials).
          delete();
    }

    private Entry getEntry(String credentials, String mediaType, String username, String id) {
        return service.
                 path("contacts").
                 path(username).
                 path(id).
                 accept(mediaType).
                 header("Authorization", credentials).
                 get(Entry.class);
    }

    private Feed getFeed(String credentials, String mediaType, String username) {
        return service.
                 path("contacts").
                 path(username).
                 accept(mediaType).
                 header("Authorization", credentials).
                 get(Feed.class);
    }

    private void postEntry(String credentials, String mediaType, String username, Contact contact) {
        Entry entry = contact.asEntry();
        try {
            service.
              path("contacts").
              path(username).
              type(mediaType).
              header("Authorization", credentials).
              post(entry);
        } catch (UniformInterfaceException e) {
            fail("Returned status " + e.getResponse().getStatus() + " instead of 201");
        }
    }

    private void putEntry(String credentials, String mediaType, String username, Contact contact) {
        Entry entry = contact.asEntry();
        try {
            service.
              path("contacts").
              path(username).
              path(contact.getId()).
              type(mediaType).
              header("Authorization", credentials).
              put(entry);
        } catch (UniformInterfaceException e) {
            fail("Returned status " + e.getResponse().getStatus() + " instead of 200");
        }
    }
*/
}
