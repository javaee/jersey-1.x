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
import com.sun.jersey.samples.contacts.server.BaseResource;
import com.sun.jersey.samples.contacts.server.Database;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.util.List;
import javax.ws.rs.core.PathSegment;

/**
 * <p>Authentication and authorization filter for the Contacts Service.</p>
 */
public class AuthFilter implements ContainerRequestFilter {


    /**
     * <p>The realm name to use in authentication challenges.</p>
     */
    private static final String REALM = "Contacts Service";


    /**
     * <p>Ensure that all requests contain proper authentication credentials,
     * and ensure that the authenticated caller is authorized to perform the
     * requested operation.</p>
     */
    public ContainerRequest filter(ContainerRequest request) {

        User user = authenticate(request);
        authorize(request, user);
        BaseResource.authenticatedUserVariable.set(user);
        return request;

    }


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


    /**
     * <p>Authorize this request based on the authenticated user and the
     * request being attempted.</p>
     *
     * @exception MappableContainerException if authorization fails
     *  (will contain an AuthorizationException)
     */
    private void authorize(ContainerRequest request, User user) {
/*
        System.out.println("REQUEST CHARACTERISTICS");
        System.out.println("          absolutePath=" + request.getAbsolutePath());
        for (MediaType mediaType : request.getAcceptableMediaTypes()) {
          System.out.println("   acceptableMediaType=" + mediaType);
        }
        System.out.println("  authenticationScheme=" + request.getAuthenticationScheme());
        System.out.println("               baseUri=" + request.getBaseUri());
        System.out.println("              language=" + request.getLanguage());
        System.out.println("             mediaType=" + request.getMediaType());
        System.out.println("                method=" + request.getMethod());
        System.out.println("                  path=" + request.getPath());
        for (PathSegment pathSegment : request.getPathSegments()) {
          System.out.println("           pathSegment=" + pathSegment.getPath());
        }
        System.out.println("            requestUri=" + request.getRequestUri());
*/
        // Administrative user can do anything
        if (Database.ADMIN_USERNAME.equals(user.getUsername())) {
            return;
        }

        // Extract path segments for this request
        List<PathSegment> pathSegments = request.getPathSegments();
        if (pathSegments.size() < 1) {
            throw new MappableContainerException(new AuthorizationException("No path to authorize\r\n"));
        }

        // All users can access the service document
        if ("service".equals(pathSegments.get(0).getPath())) {
            return;
        }

        // Non-admin users can access only their own contacts list
        else if ("contacts".equals(pathSegments.get(0).getPath())) {
            if ((pathSegments.size() > 1) &&
                (user.getUsername().equals(pathSegments.get(1).getPath()))) {
                return;
            }
        }

        // All other access is not authorized
        throw new MappableContainerException(
                new AuthorizationException("User '" + user.getUsername() + "' cannot access path '" + request.getPath() + "'\r\n"));

    }


}
