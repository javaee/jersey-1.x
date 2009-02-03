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
import com.sun.jersey.samples.contacts.models.User;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Entry;

/**
 * <p>Resource to manage an individual user for the Contacts System.</p>
 */
public class UserResource extends BaseResource {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param uriInfo Context URI information to be injected
     * @param contentHelper Context injected helper for Atom entry content
     * @param username Username of the specified user
     */
    public UserResource(UriInfo uriInfo, ContentHelper contentHelper, String username) {
        this.uriInfo = uriInfo;
        this.contentHelper = contentHelper;
        this.username = username;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Username of the specified user.</p>
     */
    private String username = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Delete the contact information for the specified user, as well as
     * all contacts owned by this user.</p>
     */
    @DELETE
    public Response delete() {
        synchronized (Database.users) {
            User user = Database.users.remove(username);
            if (user == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("User '" + username + "' not found\r\n").
                        build();
            } else {
                Database.usersUpdated = new Date();
                synchronized (Database.contacts) {
                    Database.contacts.remove(username);
                }
            }
        }
        return Response.ok().build();
    }


    /**
     * <p>Return the contact information for the specified user.  Note that
     * the <code>password</code> property will <strong>NOT</strong> be
     * included.</p>
     */
    @GET
    @Produces({"application/atom+xml",
               "application/atom+xml;type=entry",
               "application/atom+json",
               "application/atom+json;type=entry",
               "application/json",
               "application/xml",
               "text/xml"})
    public Response get() {
        Entry entry = null;
        synchronized (Database.users) {
            User user = Database.users.get(username);
            if (user == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("User '" + username + "' not found\r\n").
                        build();
            }
            entry = abdera.newEntry();
            entry.setId(user.getId());
            entry.setTitle(user.getUsername());
            entry.setUpdated(user.getUpdated());
            String uri = uriInfo.getRequestUriBuilder().build().toString();
            entry.addLink(uri, "self");
            entry.addLink(uri, "edit");
            contentHelper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, user);
        }
        return Response.ok(entry).build();
    }


    /**
     * <p>Update the contact information for the specified user.</p>
     */
    @PUT
    @Consumes({"application/atom+xml",
               "application/atom+xml;type=entry",
               "application/xml",
               "text/xml"})
    public Response put(Entry entry) {

        // Validate the incoming user information independent of the database
        User user = contentHelper.getContentEntity(entry, MediaType.APPLICATION_XML_TYPE, User.class);
        StringBuilder errors = new StringBuilder();
        if ((user.getPassword() == null) || (user.getPassword().length() < 1)) {
            errors.append("Missing 'password' property\r\n");
        }

        if (errors.length() > 0) {
            return Response.status(400).
                    type("text/plain").
                    entity(errors.toString()).build();
        }

        // Validate conditions that require locking the database
        synchronized (Database.users) {

            // Look up the original user information
            User original = Database.users.get(username);
            if (original == null) {
                return Response.status(404).
                        type("text/plain").
                        entity("User '" + username + "' does not exist\r\n").
                        build();
            }

            // Update the original user information
            original.setPassword(user.getPassword());
            original.setUpdated(new Date());
            Database.usersUpdated = new Date();
            return Response.
                    ok().
                    build();

        }

    }


}
