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

package com.sun.jersey.samples.contacts.server;

import com.sun.jersey.atom.abdera.ContentHelper;
import com.sun.jersey.samples.contacts.models.Contact;
import com.sun.jersey.samples.contacts.models.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * <p>Resource to manage the set of valid users for the Contacts System.</p>
 */
@Path("users")
public class UsersResource extends BaseResource {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param uriInfo Context URI information to be injected
     * @param contentHelper ContentHelper instance to be injected
     */
    public UsersResource(@Context UriInfo uriInfo,
                         @Context ContentHelper contentHelper) {
        this.uriInfo = uriInfo;
        this.contentHelper = contentHelper;
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the set of valid users for this application.</p>
     *
     * <p>FIXME - support pagination and filtering query parameters.</p>
     */
    @RolesAllowed("admin")
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
        feed.setId("users");
        feed.setTitle("Contacts System Users");
        feed.addLink(uriInfo.getRequestUriBuilder().build().toString(), "self");
        synchronized (Database.users) {
            feed.setUpdated(Database.usersUpdated);
            for (User user : Database.users.values()) {
                Entry entry = abdera.newEntry();
                entry.setId(user.getId());
                entry.setTitle(user.getUsername());
                entry.setUpdated(user.getUpdated());
                String uri = uriInfo.getRequestUriBuilder().path(user.getUsername()).build().toString();
                entry.addLink(uri, "self");
                entry.addLink(uri, "edit");
                contentHelper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, user);
                feed.addEntry(entry);
            }
        }
        return Response.ok(feed).build();
    }

    /**
     * <p>Create a new user based on the specified contact information.</p>
     */
    @RolesAllowed("admin")
    @POST
    @Consumes({"application/atom+xml",
               "application/atom+xml;type=entry",
               "application/xml",
               "text/xml"})
    public Response post(Entry entry) {

        // Validate the incoming user information independent of the database
        if (entry == null) {
            return Response.status(400).
                    type("text/plain").
                    entity("Missing entry in request body\r\n").build();
        }
        User user = null;
        try {
            user = contentHelper.getContentEntity(entry, MediaType.APPLICATION_XML_TYPE, User.class);
        } catch (IllegalArgumentException e) {
            return Response.status(400).
                    type("text/plain").
                    entity("Missing content element in the supplied entry\r\n").build();
        }
        StringBuilder errors = new StringBuilder();
        if ((user.getId() == null) || (user.getId().length() < 1)) {
            user.setId(UUID.randomUUID().toString());
        }
        if ((user.getPassword() == null) || (user.getPassword().length() < 1)) {
            errors.append("Missing 'password' property\r\n");
        }
        user.setUpdated(new Date());
        if ((user.getUsername() == null) || (user.getUsername().length() < 1)) {
            errors.append("Missing 'username' property\r\n");
        }
        if (errors.length() > 0) {
            return Response.status(400).
                    type("text/plain").
                    entity(errors.toString()).build();
        }

        // Validate conditions that require locking the database
        synchronized (Database.users) {

            if (Database.users.get(user.getUsername()) != null) {
                return Response.status(409).
                        type("text/plain").
                        entity("User '" + user.getUsername() + "' already exists\r\n").
                        build();
            }

            // Update the database with the new username
            Database.users.put(user.getUsername(), user);
            Database.usersUpdated = new Date();
            synchronized (Database.contacts) {
                Database.contacts.put(user.getUsername(), new ArrayList<Contact>());
                Database.contactsUpdated.put(user.getUsername(), Database.usersUpdated);
            }
            return Response.created(uriInfo.getRequestUriBuilder().path(user.getUsername()).build()).
                    build();

        }

    }


    /**
     * <p>Return an instance of {@link UserResource} configured for the
     * specified username.</p>
     *
     * @param username Username of the specified user
     */
    @Path("{username}")
    public UserResource user(@PathParam("username") String username) {
        return new UserResource(uriInfo, contentHelper, username);
    }


}
