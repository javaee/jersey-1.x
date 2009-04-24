/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.samples.https_grizzly.auth;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.security.Principal;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * Simplier version of Security filter from Atom example
 */
public class SecurityFilter implements ContainerRequestFilter {

    @Context
    UriInfo uriInfo;
    private static final String REALM = "HTTPS Example authentization";

    public ContainerRequest filter(ContainerRequest request) {
        User user = authenticate(request);
        request.setSecurityContext(new Authorizer(user));
        return request;
    }

    private User authenticate(ContainerRequest request) {
        // Extract authentication credentials
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
            throw new MappableContainerException
                    (new AuthenticationException("Authentication credentials are required", REALM));
        }
        if (!authentication.startsWith("Basic ")) {
            return null;
            // additional checks should be done here
            // "Only HTTP Basic authentication is supported"
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(authentication)).split(":");
        if (values.length < 2) {
            return null;
            // additional checks should be done here
            // "Invalid syntax for username and password"
        }
        String username = values[0];
        String password = values[1];
        if ((username == null) || (password == null)) {
            return null;
            // additional checks should be done here
            // "Missing username or password"
        }

        // Validate the extracted credentials
        User user = null;

        if (username.equals("user") && password.equals("password")) {
            user = new User("user", "user");
            System.out.println("USER AUTHENTICATED");
        //        } else if (username.equals("admin") && password.equals("adminadmin")) {
        //            user = new User("admin", "admin");
        //            System.out.println("ADMIN AUTHENTICATED");
        } else {
            System.out.println("USER NOT AUTHENTICATED");
            throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
        }
        return user;
    }

    public class Authorizer implements SecurityContext {

        private User user;
        private Principal principal;

        public Authorizer(final User user) {
            this.user = user;
            this.principal = new Principal() {

                public String getName() {
                    return user.username;
                }
            };
        }

        public Principal getUserPrincipal() {
            return this.principal;
        }

        public boolean isUserInRole(String role) {
            return (role.equals(user.role));
        }

        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }

    public class User {

        public String username;
        public String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }
}
