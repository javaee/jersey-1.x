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

package com.sun.jersey.samples.contacts.server;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.samples.contacts.models.User;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * <p>Unit tests for user access in the Contacts Service.</p>
 */
public class UsersTest extends AbstractTest {   

    @Override
    public void setUp() throws Exception {  
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception { 
        super.setUp();
    }

    private static final QName USER_QNAME = new QName("http://example.com/contacts", "user");
    private static final QName USERNAME_QNAME = new QName("http://example.com/contacts", "username");
    private static final QName PASSWORD_QNAME = new QName("http://example.com/contacts", "password");

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

    @Test
    public void testGetEntryNegative() {
        String credentials = adminCredentials();
        // Negative test -- invalid username
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            try {
                Entry entry = getEntry(credentials, mediaType, "foo");
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

    @Test
    public void testGetEntryPositive() {
        String credentials = adminCredentials();
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            try {
                Entry entry = getEntry(credentials, mediaType, "admin");
                checkAdminEntry(entry, mediaType);
            } catch (UniformInterfaceException e) {
                e.printStackTrace(System.out);
                fail("Exception getting feed for media type " + mediaType + ": " + e);
            }
        }
    }

    @Test
    public void testGetFeed() {
        String credentials = adminCredentials();
        for (String mediaType : FEED_MEDIA_TYPES) {
            try {
                Feed feed = getFeed(credentials, mediaType);
                assertNotNull("Got feed for media type " + mediaType, feed);
                assertEquals("Feed id for media type " + mediaType, "users", feed.getId().toString());
                assertEquals("Feed title for media type " + mediaType, "Contacts System Users", feed.getTitle());
                assertEquals("Feed self link for media type " + mediaType, 1, feed.getLinks("self").size());
                assertEquals("Feed entries for media type " + mediaType, 1, feed.getEntries().size());
                assertEquals("Feed entry title for media type " + mediaType, "admin", feed.getEntries().get(0).getTitle());
                assertNotNull("Feed updated for media type " + mediaType, feed.getUpdated());
                checkAdminEntry(feed.getEntries().get(0), mediaType);
            } catch (UniformInterfaceException e) {
                e.printStackTrace(System.out);
                fail("Exception getting feed for media type " + mediaType + ": " + e);
            }
        }
    }

    @Test
    public void testPostFeedPositive() {
        String credentials = adminCredentials();
        Feed feed = null;
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            feed = getFeed(credentials, "application/atom+xml;type=feed");
            assertEquals("Before entries for media type " + mediaType, 1, feed.getEntries().size());
            createUser(credentials, mediaType, "newuser", "newpass");
            feed = getFeed(credentials, "application/atom+xml;type=feed");
            assertEquals("After entries for media type " + mediaType, 2, feed.getEntries().size());
            Entry entry = getEntry(credentials, mediaType, "newuser");
            assertNotNull("New id for media type " + mediaType, entry.getId());
            assertEquals("New type for media type " + mediaType, "newuser", entry.getTitle());
            User user = helper.getContentEntity(entry, User.class);
            assertEquals("Username for media type " + mediaType, "newuser", user.getUsername());
            assertEquals("Password for media type " + mediaType, "newpass", user.getPassword());
            deleteUser(credentials, "newuser");
            feed = getFeed(credentials, "application/atom+xml;type=feed");
            assertEquals("Cleaned entries for media type " + mediaType, 1, feed.getEntries().size());
        }
    }

    @Test
    public void testPutEntryPositive() {
        String credentials = adminCredentials();
        for (String mediaType : ENTRY_MEDIA_TYPES) {
            createUser(credentials, mediaType, "newuser", "oldpass");
            Entry entry = getEntry(credentials, mediaType, "newuser");
            User user = helper.getContentEntity(entry, MediaType.APPLICATION_XML_TYPE, User.class);
            assertEquals("Old password for media type " + mediaType, "oldpass", user.getPassword());
            user.setPassword("newpass");
            helper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, user);
            putEntry(credentials, mediaType, user);
            entry = getEntry(credentials, mediaType, "newuser");
            user = helper.getContentEntity(entry, MediaType.APPLICATION_XML_TYPE, User.class);
            assertEquals("new password for media type " + mediaType, "newpass", user.getPassword());
            deleteUser(credentials, "newuser");
        }
    }

    private void checkAdminEntry(Entry entry, String mediaType) {
        assertNotNull("Got entry for media type " + mediaType, entry);
        assertNotNull("Entry id for media type " + mediaType, entry.getId());
        assertEquals("Entry title for media type " + mediaType, "admin", entry.getTitle());
        assertNotNull("Entry self link for media type " + mediaType, entry.getLink("self"));
        assertNotNull("Entry edit link for media type " + mediaType, entry.getLink("edit"));
        assertNotNull("Entry updated for media type " + mediaType, entry.getUpdated());
        checkAdminUserContent(entry.getContentElement(), mediaType);
    }

    private void checkAdminUserContent(Element content, String mediaType) {
        assertNotNull("Content element for media type " + mediaType, content);
        Element user = content.getFirstChild(USER_QNAME);
        assertNotNull("User element for media type " + mediaType, user);
        Element password = user.getFirstChild(PASSWORD_QNAME);
        password = getElement(user, "password"); // FIXME - Abdera bug not namespacing child elements?
        assertNotNull("Password element for media type " + mediaType, password);
        assertEquals("Password text for media type " + mediaType, "password", password.getText());
        Element username = user.getFirstChild(USERNAME_QNAME);
        username = getElement(user, "username"); // FIXME - Abdera bug not namespacing child elements?
        assertNotNull("Username element for media type " + mediaType, username);
        assertEquals("Username text for media type " + mediaType, "admin", username.getText());
    }

    private Element getElement(Element parent, String localPart) {
        for (Element child : parent.getElements()) {
            if (localPart.equals(child.getQName().getLocalPart())) {
                return child;
            }
        }
        return null;
    }

    private Entry getEntry(String credentials, String mediaType, String username) {
        return service.
                 path("users").
                 path(username).
                 accept(mediaType).
                 header("Authorization", credentials).
                 get(Entry.class);
    }

    private Feed getFeed(String credentials, String mediaType) {
        return service.
                 path("users").
                 accept(mediaType).
                 header("Authorization", credentials).
                 get(Feed.class);
    }

    private void putEntry(String credentials, String mediaType, User user) {
        Entry entry = abdera.newEntry();
        entry.setTitle(user.getUsername());
        entry.setId(user.getId());
        entry.setUpdated(user.getUpdated());
        helper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, user);
        try {
            service.
              path("users").
              path(user.getUsername()).
              type(mediaType).
              header("Authorization", credentials).
              put(entry);
        } catch (UniformInterfaceException e) {
            fail("Returned status " + e.getResponse().getStatus() + " instead of 200");
        }
    }

}
