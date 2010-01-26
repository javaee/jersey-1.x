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
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Entry;

/**
 * <p>Resource to manage an individual contact for the Contacts System.</p>
 */
public class ContactResource extends BaseResource {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param uriInfo Context URI information to be injected
     * @param username Username of the specified user
     * @param id Id of the specified contact
     */
    public ContactResource(UriInfo uriInfo, String username, String id) {
        this.uriInfo = uriInfo;
        this.username = username;
        this.id = id;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Id of the specified contact.</p>
     */
    private String id = null;


    /**
     * <p>Username of the specified user.</p>
     */
    private String username = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Delete the contact information for the specified contact.</p>
     */
    @RolesAllowed("user")
    @DELETE
    public Response delete() {
        synchronized (Database.contacts) {
            List<Contact> contacts = Database.contacts.get(username);
            if (contacts == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("No contacts for user '" + username + "'").
                        build();
            }
            for (int i = 0; i < contacts.size(); i++) {
                if (id.equals(contacts.get(i).getId())) {
                    contacts.remove(i);
                    Database.contactsUpdated.put(username, new Date());
                    return Response.ok().build();
                }
            }
        }
        return Response.status(404).
                type("text/plain").
                entity("No contact for user '" + username + "'").
                build();
    }


    /**
     * <p>Return the contact information for the specified contact.</p>
     */
    @RolesAllowed("user")
    @GET
    @Produces({"application/atom+xml",
               "application/atom+xml;type=entry",
               "application/atom+json",
               "application/atom+json;type=entry",
               "application/json",
               "application/xml",
               "text/xml"})
    public Response get() {
        synchronized (Database.contacts) {
            List<Contact> contacts = Database.contacts.get(username);
            if (contacts == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("No contacts for user '" + username + "'").
                        build();
            }
            for (int i = 0; i < contacts.size(); i++) {
                if (id.equals(contacts.get(i).getId())) {
                    Entry entry = contacts.get(i).asEntry();
                    String uri = uriInfo.getRequestUriBuilder().build().toString();
                    entry.addLink(uri, "self");
                    entry.addLink(uri, "edit");
                    return Response.ok(entry).build();
                }
            }
        }
        return Response.status(404).
                type("text/plain").
                entity("No contact for user '" + username + "'").
                build();
    }


    /**
     * <p>Update the contact information for the specified contact.</p>
     */
    @RolesAllowed("user")
    @PUT
    @Consumes({"application/atom+xml",
               "application/atom+xml;type=entry",
               "application/xml",
               "text/xml"})
    public Response put(Entry entry) {

        // Validate the incoming user information independent of the database
        Contact contact = Contact.fromEntry(entry);
        StringBuilder errors = new StringBuilder();
        if (contact.getContent() == null) {
            errors.append("Missing 'content' property\r\n");
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

            // Look up the original contact information
            List<Contact> contacts = Database.contacts.get(username);
            if (contacts == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("No contacts for user '" + username + "'").
                        build();
            }
            Contact original = null;
            for (int i = 0; i < contacts.size(); i++) {
                if (id.equals(contacts.get(i).getId())) {
                    original = contacts.get(i);
                }
            }
            if (original == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("No contact for user '" + username + "'\r\n").
                        build();
            }

            // Update the original contact information
            original.updateFrom(contact);
            return Response.
                    ok().
                    build();

        }

    }


}
