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

import com.sun.jersey.samples.contacts.models.User;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;

/**
 * <p>Resource class to serve the {@link Service} document describing this
 * AtomPub based web service.  It is assumed that authentication and
 * authorization have been taken care of externally.</p>
 */
@Path("service")
public class ServiceResource extends BaseResource {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param uriInfo Context URI information to be injected
     */
    public ServiceResource(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the {@link Service} document describing this
     * AtomPub based web service.  The administrative user gets to see
     * all contact list feeds plus all administrative feeds.  Other users
     * get to see only their own contact list feed (FIXME - allow sharing
     * of contact lists).</p>
     */
    @GET
    @Produces({ "application/atomsvc+xml", "application/xml", "text/xml",
                "application/atomsvc+json", "application/json" })
    public Response get() {

        User authenticatedUser = getAuthenticatedUser();
        Service service = abdera.newService();
        Workspace contactsWorkspace = service.addWorkspace("Contact List Feeds");
        synchronized (Database.users) {
            if (Database.ADMIN_USERNAME.equals(authenticatedUser.getUsername())) {
                for (User user : Database.users.values()) {
                    addContactList(contactsWorkspace, user);
                }
            } else {
                addContactList(contactsWorkspace, authenticatedUser);
            }
        }
        if (Database.ADMIN_USERNAME.equals(authenticatedUser.getUsername())) {
            Workspace adminWorkspace = service.addWorkspace("Administrative Feeds");
            Collection usersCollection = adminWorkspace.addCollection("User List", uriInfo.getBaseUri() + "users");
        }
        return Response.ok(service).build();
    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Add a contact list collection for the specified user to the
     * specified workspace.</p>
     *
     * @param workspace Workspace to which the collection should be added
     * @param user User for which to add a contact list collection
     */
    private void addContactList(Workspace workspace, User user) {
        String uri = uriInfo.getBaseUriBuilder().path("contacts").path(user.getUsername()).build().toString();
        workspace.addCollection("Username '" + user.getUsername() + "' Contact List", uri);
    }


}
