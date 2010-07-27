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

package com.sun.jersey.samples.contacts.server;

import com.sun.jersey.samples.contacts.models.Contact;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * <p>Resource to manage the set of valid contacts for the Contacts System.</p>
 */
@Path("contacts/{username}")
public class ContactsResource extends BaseResource {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param uriInfo Context URI information to be injected
     * @param username Username of the user for which to manage contacts
     */
    public ContactsResource(@Context UriInfo uriInfo,
                            @PathParam("username") String username) {
        this.uriInfo = uriInfo;
        this.username = username;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Username of the user whose contacts are to be managed.</p>
     */
    private String username = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the set of contacts for this user.</p>
     *
     * <p>FIXME - support pagination and filtering query parameters.</p>
     */
    @RolesAllowed("user")
    @GET
    @Produces({"application/atom+xml",
               "application/atom+xml;type=feed",
               "application/atom+json",
               "application/atom+json;type=feed",
               "application/json",
               "application/xml",
               "text/xml"})
    public Response get() {
        Feed feed = abdera.newFeed();
        // NOTE - RFC 4287 requires a feed to have id, title, and updated properties
        feed.setId("contacts");
        feed.setTitle("Contacts System Contacts for user '" + username + "'");
        feed.addLink(uriInfo.getRequestUriBuilder().build().toString(), "self");
        synchronized (Database.contacts) {
            feed.setUpdated(Database.contactsUpdated.get(username));
            List<Contact> contacts = Database.contacts.get(username);
            if (contacts == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("No contacts for user '" + username + "'").
                        build();
            }
            for (Contact contact : contacts) {
                Entry entry = contact.asEntry();
                String uri = uriInfo.getRequestUriBuilder().path(contact.getId()).build().toString();
                entry.addLink(uri, "self");
                entry.addLink(uri, "edit");
                feed.addEntry(entry);
            }
        }
        return Response.ok(feed).build();
    }

    /**
     * <p>Create a new contact based on the specified contact information.</p>
     */
    @RolesAllowed("user")
    @POST
    @Consumes({"application/atom+xml",
               "application/atom+xml;type=entry",
               "application/xml",
               "text/xml"})
    public Response post(Entry entry) {

        // Validate the incoming user information independent of the database
        Contact contact = Contact.fromEntry(entry);
        StringBuilder errors = new StringBuilder();
        if (contact.getContent() == null) {
            errors.append("Missing 'content' property\r\n");
        }
        if ((contact.getId() == null) || (contact.getId().length() < 1)) {
            contact.setId(UUID.randomUUID().toString());
        }
        if ((contact.getName() == null) || (contact.getName().length() < 1)) {
            errors.append("Missing 'name' property\r\n");
        }
        contact.setUpdated(new Date());

        if (errors.length() > 0) {
            return Response.status(400).
                    type("text/plain").
                    entity(errors.toString()).build();
        }

        // Validate conditions that require locking the database
        synchronized (Database.contacts) {

            // Verify user is valid and no contact with this id exists
            List<Contact> contacts = Database.contacts.get(username);
            if (contacts == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("No contacts for user '" + username + "'\r\n").build();
            }
            for (Contact existing : contacts) {
                if (existing.getId().equals(contact.getId())) {
                    return Response.status(409).
                            type("text/plain").
                            entity("Contact with this ID already exists\r\n").build();
                }
            }

            // Update the database with the new contact
            contacts.add(contact);
            return Response.created(uriInfo.getRequestUriBuilder().path(contact.getId()).build()).
                    build();

        }

    }


    /**
     * <p>Return an instance of {@link ContactResource} configured for the
     * specified contact.</p>
     *
     * @param id Id of the specified contact
     */
    @Path("{id}")
    public ContactResource user(@PathParam("id") String id) {
        return new ContactResource(uriInfo, username, id);
    }


}
