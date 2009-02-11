/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.contacts.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.atom.abdera.ContentHelper;
import com.sun.jersey.samples.contacts.models.Contact;
import com.sun.jersey.samples.contacts.models.User;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * <p>Simplified Java client for the <code>AtomPub Contacts Server</code>.</p>
 */
public class ContactsClient {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a fully configured client instance.</p>
     *
     * @param uri URI of the deployed Contacts Service to contact
     * @param username Username of the client calling this service
     * @param password Password of the client calling this service
     */
    public ContactsClient(String uri, String username, String password) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        service = client.resource(uri);
        authentication = "Basic " + encodeCredentialsBasic(username, password);
        helper = new ContentHelper(client.getProviders());
    }


    // -------------------------------------------------------- Static Variables


    /**
     * <p>HTTP header for sending authentication credentials.</p>
     */
    private static final String AUTHENTICATION_HEADER = "Authorization";


    /**
     * <p>Default media type for content element.</p>
     */
    private static final MediaType CONTENT_MEDIA_TYPE = MediaType.APPLICATION_XML_TYPE;


    /**
     * <p>Default media type for entry entities.</p>
     */
    private static final MediaType ENTRY_MEDIA_TYPE;
    static {
        Map<String,String> params = new HashMap<String,String>(1);
        params.put("type", "entry");
        ENTRY_MEDIA_TYPE = new MediaType("application", "xml", params);
    }


    /**
     * <p>Default media type for feed entities.</p>
     */
    private static final MediaType FEED_MEDIA_TYPE;
    static {
        Map<String,String> params = new HashMap<String,String>(1);
        params.put("type", "feed");
        FEED_MEDIA_TYPE = new MediaType("application", "xml", params);
    }


    /**
     * <p>Abdera singleton instance for this application.</p>
     */
    private static Abdera abdera = Abdera.getInstance();


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>HTTP authentication header value to include on each request.</p>
     */
    private String authentication = null;


    /**
     * <p>The {@link ContentHelper} instance we will use for converting
     * to or from custom content XML representations.</p>
     */
    private ContentHelper helper = null;


    /**
     * <p>Client proxy for accessing service resources.</p>
     */
    private WebResource service = null;


    // ------------------------------------------------- Contacts Public Methods


    /**
     * <p>Add the specified contact to the set of registered contacts for
     * the specified username.  Return the URI of the newly created contact.</p>
     *
     * @param username Username of the user to add a contact for
     * @param contact Contact information forthe new contact
     *
     * @exception IllegalArgumentException if a contact with this id
     *  is already registered
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public String createContact(String username, Contact contact) {
        try {
            ClientResponse response = service.
              path("contacts").
              path(username).
              type(ENTRY_MEDIA_TYPE).
              header(AUTHENTICATION_HEADER, authentication).
              post(ClientResponse.class, contact.asEntry());
            return response.getLocation().toString();
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 409) {
                throw new IllegalArgumentException("Username '" + username +
                        "' already has a contact with this id registered");
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>Delete the specified contact for the specified user.</p>
     *
     * @param username Username of the user for which to delete a contact
     * @param id Contact identifier of the contact to delete
     *
     * @exception IllegalArgumentException if a user with this username
     *  or a contact with this id is not currently registered
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public void deleteContact(String username, String id) {
        try {
            service.
              path("contacts").
              path(username).
              path(id).
              header(AUTHENTICATION_HEADER, authentication).
              delete();
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + username +
                        "' and/or contact id '" + id + "' does not exist");
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>Find and return the contact with the specified id for the user
     * with the specified username, if any.</p>
     *
     * @param username Username of the user to return a contact for
     * @param id Id of the contact to return
     *
     * @exception IllegalArgumentException if the specified username does not
     *  identify a valid user or the specified id does not identify a
     *  valid contact for that user
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public Contact findContact(String username, String id) {
        try {
            Entry entry = service.
                    path("contacts").
                    path(username).
                    path(id).
                    accept(ENTRY_MEDIA_TYPE).
                    header(AUTHENTICATION_HEADER, authentication).
                    get(Entry.class);
            return Contact.fromEntry(entry);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + username +
                        "' and/or contact id '" + id + "' does not exist", e);
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>Find and return all contacts for the specified username
     * registered with the Contacts service.</p>
     *
     * @param username Username for which to retrieve contacts
     *
     * @exception IllegalArgumentException if the specified user
     *  does not exist
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public List<Contact> findContacts(String username) {
        Feed feed = null;
        try {
            feed = service.
               path("contacts").
               path(username).
               accept(FEED_MEDIA_TYPE).
               header(AUTHENTICATION_HEADER, authentication).
               get(Feed.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + username + "' does not exist");
            } else {
                throw e;
            }
        }
        List<Contact> results = new ArrayList<Contact>();
        for (Entry entry : feed.getEntries()) {
            results.add(Contact.fromEntry(entry));
        }
        return results;
    }


    /**
     * <p>Update the specified contact for the specified user.</p>
     *
     * @param username Username for which to update a contact
     * @param contact Contact information to be updated
     *
     * @exception IllegalArgumentException if a contact with this id
     *  is not already registered
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public void updateContact(String username, Contact contact) {
        try {
            service.
              path("contacts").
              path(username).
              path(contact.getId()).
              type(ENTRY_MEDIA_TYPE).
              header(AUTHENTICATION_HEADER, authentication).
              put(ClientResponse.class, contact.asEntry());
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + username +
                        "' and/or contact id '" + contact.getId() + "' does not exist");
            } else {
                throw e;
            }
        }
    }


    // ---------------------------------------------------- Users Public Methods


    /**
     * <p>Add the specified user to the Contacts System.  Returns the URI
     * of the newly created user.</p>
     *
     * @param user User information to be added
     *
     * @exception IllegalArgumentException if a user with this username
     *  already exists
     * @exception IllegalStateException if you are not authorized
     *  to perform this request
     */
    public String createUser(User user) {
        try {
            Entry entry = abdera.newEntry();
            entry.setTitle(user.getUsername());
            helper.setContentEntity(entry, CONTENT_MEDIA_TYPE, user);
            ClientResponse response = service.
              path("users").
              type(ENTRY_MEDIA_TYPE).
              header(AUTHENTICATION_HEADER, authentication).
              post(ClientResponse.class, entry);
            return response.getLocation().toString();
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 409) {
                throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists");
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>Delete the user with the specified name, and all that user's contacts.</p>
     *
     * @param username Username of the user to delete
     *
     * @exception IllegalArgumentException if no such user exists
     * @exception IllegalStateException if you are not authorized to
     *  perform this request
     */
    public void deleteUser(String username) {
        try {
            service.
              path("users").
              path(username).
              header(AUTHENTICATION_HEADER, authentication).
              delete();
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + username + "' does not exist");
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>Find and return the user with the specified username.</p>
     *
     * @param username Username of the requested user
     *
     * @exception IllegalArgumentException if no such user exists
     * @exception IllegalStateException if you are not authorized to
     *  perform this request
     */
    public User findUser(String username) {
        try {
            Entry entry = service.
                    path("users").
                    path(username).
                    accept(ENTRY_MEDIA_TYPE).
                    header(AUTHENTICATION_HEADER, authentication).
                    get(Entry.class);
            return helper.getContentEntity(entry, User.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + username + "' does not exist");
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>Find and return all registered users.</p>
     *
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public List<User> findUsers() {
        Feed feed = null;
        try {
            feed = service.
               path("users").
               accept(FEED_MEDIA_TYPE).
               header(AUTHENTICATION_HEADER, authentication).
               get(Feed.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else {
                throw e;
            }
        }
        List<User> results = new ArrayList<User>();
        for (Entry entry : feed.getEntries()) {
            results.add(helper.getContentEntity(entry, User.class));
        }
        return results;
    }


    /**
     * <p>Update the specified user.</p>
     *
     * @param user User information to be updated
     *
     * @exception IllegalArgumentException if no such user exists
     * @exception IllegalStateException if the caller is not authorized to
     *  perform this request
     */
    public void updateUser(User user) {
        try {
            Entry entry = abdera.newEntry();
            entry.setTitle(user.getUsername());
            helper.setContentEntity(entry, ENTRY_MEDIA_TYPE, user);
            service.
              path("users").
              path(user.getUsername()).
              type(ENTRY_MEDIA_TYPE).
              header(AUTHENTICATION_HEADER, authentication).
              put(ClientResponse.class, entry);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 401) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 403) {
                throw new IllegalStateException(e);
            } else if (e.getResponse().getStatus() == 404) {
                throw new IllegalArgumentException("Username '" + user.getUsername() + "' does not exist");
            } else {
                throw e;
            }
        }
    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Convenience string for Base 64 encoding.</p>
     */
    private static final String BASE64_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789+/";


    /**
     * <p>Encode the specified credentials into a String as required by
     * HTTP Basic Authentication (<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>).</p>
     *
     * @param username Username to be encoded
     * @param password Password to be encoded
     */
    public String encodeCredentialsBasic(String username, String password) {

        String encode = username + ":" + password;
        int paddingCount = (3 - (encode.length() % 3)) % 3;
        encode += "\0\0".substring(0, paddingCount);
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < encode.length(); i += 3) {
            int j = (encode.charAt(i) << 16) + (encode.charAt(i + 1) << 8) + encode.charAt(i + 2);
            encoded.append(BASE64_CHARS.charAt((j >> 18) & 0x3f));
            encoded.append(BASE64_CHARS.charAt((j >> 12) & 0x3f));
            encoded.append(BASE64_CHARS.charAt((j >> 6) & 0x3f));
            encoded.append(BASE64_CHARS.charAt(j & 0x3f));
        }
        return encoded.toString();

    }

    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).build();
    }

    public static final URI BASE_URI = getBaseURI();

    /**
     *  This is only a sample main method, you can experiment on your own by using above implemented methods.
     */
    public static void main(String[] args) {

        String uri, username, password;

        if (args.length < 3) {
            uri = BASE_URI.toString();
            username = "admin";
            password = "password";
        } else {
            uri = args[0];
            username = args[1];
            password = args[2];
        }
        
        ContactsClient client = new ContactsClient(uri, username, password);
        System.out.println(String.format("List of users: %s", client.findUsers()));
    }
}
