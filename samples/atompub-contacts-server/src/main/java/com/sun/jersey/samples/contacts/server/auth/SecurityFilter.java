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

package com.sun.jersey.samples.contacts.server.auth;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.samples.contacts.models.User;
import com.sun.jersey.samples.contacts.server.Database;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.security.Principal;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * <p>A Jersey {@link ContainerRequestFilter} that provides a {@link SecurityContext}
 * for all requests processed by this application.  All defined users are
 * considered to possess role <code>user</code>, which authorizes read/write
 * access to the contacts list for that user.  The user named <code>admin</code>
 * is also considered to possess role <code>admin</code>, which authorizes
 * read/write access to the contacts list for any user, as well as read/write
 * access to the list of defined users.</p>
 */
public class SecurityFilter implements ContainerRequestFilter {


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The realm name to use in authentication challenges.</p>
     */
    private static final String REALM = "Contacts Service";


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The URI information for this request.</p>
     */
    @Context
    UriInfo uriInfo;


    // ------------------------------------------ ContainerRequestFilter Methods


    /**
     * <p>Authenticate the user for this request, and add a security context
     * so that role checking can be performed.</p>
     *
     * @param request The request we re processing
     * @return the decorated request
     * @exception AuthenticationException if authentication credentials
     *  are missing or invalid
     */
    public ContainerRequest filter(ContainerRequest request) {
        User user = authenticate(request);
        request.setSecurityContext(new Authorizer(user));
//        System.out.println("CURRENT USER IS " + user.getUsername());
        return request;
    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Perform the required authentication checks, and return the
     * {@link User} instance for the authenticated user.</p>
     *
     * @exception MappableContainerException if authentication fails
     *  (will contain an AuthenticationException)
     */
    private User authenticate(ContainerRequest request) {

        // Extract authentication credentials
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
            throw new MappableContainerException
                    (new AuthenticationException("Authentication credentials are required\r\n", REALM));
        }
        if (!authentication.startsWith("Basic ")) {
            throw new MappableContainerException
                    (new AuthenticationException("Only HTTP Basic authentication is supported\r\n", REALM));
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(authentication)).split(":");
        if (values.length < 2) {
            throw new MappableContainerException
                    (new AuthenticationException("Invalid syntax for username and password\r\n", REALM));
        }
        String username = values[0];
        String password = values[1];
        if ((username == null) || (password == null)) {
            throw new MappableContainerException
                    (new AuthenticationException("Missing username or password\r\n", REALM));
        }

        // Validate the extracted credentials
        User user = null;
        synchronized (Database.users) {
            user = Database.users.get(username);
            if (user == null) {
                throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
            } else if (!password.trim().equals(user.getPassword().trim())) {
                throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
            }
        }

        // Return the validated user
        return user;

    }


    // --------------------------------------------------------- Support Classes


    /**
     * <p>SecurityContext used to perform authorization checks.</p>
     */
    public class Authorizer implements SecurityContext {

        public Authorizer(final User user) {
            this.principal = new Principal() {
                public String getName() {
                    return user.getUsername();
                }
            };
        }

        private Principal principal;

        public Principal getUserPrincipal() {
            return this.principal;
        }

        /**
         * <p>Determine whether the authenticated user possesses the requested
         * role, according to the following rules:</p>
         * <ul>
         * <li>User <code>admin</code> has the <code>admin</code> and
         *     <code>user</code> roles unconditionally.</li>
         * <li>All other users have the <code>user</code> role <strong>ONLY</strong>
         *     for URIs that specify their own contact lists.</li>
         * </ul>
         *
         * @param role Role to be checked
         */
        public boolean isUserInRole(String role) {
            if ("admin".equals(role)) {
//                System.out.println("isUserInRole(admin) ==> " + "admin".equals(this.principal.getName()));
                return "admin".equals(this.principal.getName());
            } else if ("user".equals(role)) {
                if ("admin".equals(this.principal.getName())) {
//                    System.out.println("isUserInRole(user) ==> true for admin unconditionally");
                    return true;
                }
                List<PathSegment> pathSegments = uriInfo.getPathSegments();
                if ((pathSegments.size() >= 2) &&
                    "contacts".equals(pathSegments.get(0).getPath()) &&
                    this.principal.getName().equals(pathSegments.get(1).getPath())) {
//                    System.out.println("isUserInRole(user) ==> true for this user");
                    return true;
                } else {
//                    System.out.println("isUserInRole(user) ==> false for this user");
                    return false;
                }
            }
//            System.out.println("isUserInRole(" + role + ") ==> false unconditionally");
            return false;
        }

        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }


}
