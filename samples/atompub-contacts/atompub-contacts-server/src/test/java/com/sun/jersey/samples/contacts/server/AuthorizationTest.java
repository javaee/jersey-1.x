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

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.axiom.om.util.Base64;

/**
 * <p>Unit tests for authorization in the Contacts Service.</p>
 */
public class AuthorizationTest extends AbstractTest {

    public AuthorizationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createUser(adminCredentials(), "application/atom+xml;type=entry", "newuser", "newpass");
    }

    @Override
    protected void tearDown() throws Exception {
        deleteUser(adminCredentials(), "newuser");
        super.tearDown();
    }

    // Partial URIs that should always allow access by the "admin" user
    private static final String[][] ADMIN_POSITIVE_URIS = {
        { "users" },
        { "users", "admin" },
        { "users", "newuser" },
        { "contacts", "admin" },
        { "contacts", "newuser" },
    };

    // Entity classes corresponding to ADMIN_POSITIVE_URIS
    private static final Class[] ADMIN_POSITIVE_CLASSES = {
        Feed.class,
        Entry.class,
        Entry.class,
        Feed.class,
        Feed.class,
    };

    // Partial URIs that should never allow access by the "newuser" user
    private static final String[][] NEWUSER_NEGATIVE_URIS = {
        { "users" },
        { "users", "admin" },
        { "users", "newuser" },
        { "contacts", "admin" },
    };

    // Entity classes corresponding to NEWUSER_NEGATIVE_URIS
    private static final Class[] NEWUSER_NEGATIVE_CLASSES = {
        Feed.class,
        Entry.class,
        Entry.class,
        Feed.class,
    };

    // Partial URIs that should always allow access by the "newuser" user
    private static final String[][] NEWUSER_POSITIVE_URIS = {
        { "contacts", "newuser" },
    };

    // Entity classes corresponding to NEWUSER_POSITIVE_URIS
    private static final Class[] NEWUSER_POSITIVE_CLASSES = {
        Feed.class,
    };

    public void testAuthorizationAdminPositive() {
        String credentials = adminCredentials();
        for (int i = 0; i < ADMIN_POSITIVE_URIS.length; i++) {
            WebResource resource = resource(ADMIN_POSITIVE_URIS[i]);
            try {
                Object result = resource.
                  header("Authorization", credentials).
                  get(ADMIN_POSITIVE_CLASSES[i]);
            } catch (UniformInterfaceException e) {
                fail("Status was " + e.getResponse().getStatus() + " instead of 200 for path '" + path(ADMIN_POSITIVE_URIS[i]) + "'");
            }
        }
    }

    public void testAuthorizationUserNegative() {
        String credentials = userCredentials("newuser", "newpass");
        for (int i = 0; i < NEWUSER_NEGATIVE_URIS.length; i++) {
            WebResource resource = resource(NEWUSER_NEGATIVE_URIS[i]);
            try {
                Object result = resource.
                  header("Authorization", credentials).
                  get(NEWUSER_NEGATIVE_CLASSES[i]);
                fail("Should have returned 403 instead of 200 for path '" + path(NEWUSER_NEGATIVE_URIS[i]) + "'");
            } catch (UniformInterfaceException e) {
                if (e.getResponse().getStatus() == 403) {
                    // expected result
                } else {
                    fail("Status was " + e.getResponse().getStatus() + " instead of 403 for path '" + path(NEWUSER_NEGATIVE_URIS[i]) + "'");
                }
            }
        }
    }

    public void testAuthorizationUserPositive() {
        String credentials = userCredentials("newuser", "newpass");
        for (int i = 0; i < NEWUSER_POSITIVE_URIS.length; i++) {
            WebResource resource = resource(NEWUSER_POSITIVE_URIS[i]);
            try {
                Object result = resource.
                  header("Authorization", credentials).
                  get(NEWUSER_POSITIVE_CLASSES[i]);
            } catch (UniformInterfaceException e) {
                fail("Status was " + e.getResponse().getStatus() + " instead of 200 for path '" + path(NEWUSER_POSITIVE_URIS[i]) + "'");
            }
        }
    }

}
