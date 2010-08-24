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

package com.sun.jersey.samples.contacts.server;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.samples.contacts.models.Contact;
import java.util.Date;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * <p>Unit tests for contacts access in the Contacts Service.</p>
 */
public class ContactsTest extends AbstractTest {

    public ContactsTest(String testName) {
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

    private static final Abdera abdera = Abdera.getInstance();

    // Media types to get entries
    private static final String[] ENTRY_MEDIA_TYPES = {
        "application/atom+xml",
        "application/atom+xml;type=entry",
        "application/xml",
        "text/xml",
    };

    // Media types to get feeds
    private static final String[] FEED_MEDIA_TYPES = {
        "application/atom+xml",
        "application/atom+xml;type=feed",
        "application/xml",
        "text/xml",
    };

    public void testGetEntryNegative() {
        String credentials = adminCredentials();
        // Negative test -- invalid username
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            try {
                Entry entry = getEntry(credentials, mediaType, "foo", "bar");
                fail("Should have returned 404 for media type " + mediaType);
            } catch (UniformInterfaceException e) {
                if (e.getResponse().getStatus() == 404) {
                    // Expected result
                } else {
                    fail("Returned status " + e.getResponse().getStatus() + " instead of 404 for media type " + mediaType);
                }
            }
        }
    }

    public void testGetEntryPositive() {
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
                assertEquals("Feed title for media type " + mediaType, "Contacts System Contacts for user 'admin'", feed.getTitle());
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

}
