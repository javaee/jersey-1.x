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

import com.sun.jersey.samples.contacts.models.Contact;
import com.sun.jersey.samples.contacts.models.EmailAddress;
import com.sun.jersey.samples.contacts.models.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>The "database" of user and contact information (not persistent).</p>
 */
public class Database {


    /**
     * <p>The username of the administrative user that is created
     * upon initialization.</p>
     */
    public static final String ADMIN_USERNAME = "admin";


    /**
     * <p>The password of the administrative user that is created
     * upon initialization.</p>
     */
    private static final String ADMIN_PASSWORD = "password";


    /**
     * <p>Map of lists of contacts, keyed by the owning username.</p>
     *
     * <p><strong>WARNING</strong> - All access to this map must be
     * appropriately synchronized.</p>
     */
    public static final Map<String,List<Contact>> contacts =
            new HashMap<String,List<Contact>>();
    static {
        contacts.put(ADMIN_USERNAME, new ArrayList<Contact>());
    }


    /**
     * <p>Map of last-updated timestamps for each user's contact list,
     * keyed by the owning username.</p>
     *
     * <p><strong>WARNING</strong> - All access to this map must be
     * appropriately synchronized.</p>
     */
    public static final Map<String,Date> contactsUpdated =
            new HashMap<String,Date>();
    static {
        contactsUpdated.put(ADMIN_USERNAME, new Date());
    }


    /**
     * <p>Map of the valid users of this application, keyed by username.</p>
     *
     * <p><strong>WARNING</strong> - All access to this map must be
     * appropriately synchronized.</p>
     */
    public static final Map<String,User> users =
            new HashMap<String,User>();
    static {
        User administrator = new User();
        administrator.setUsername(ADMIN_USERNAME);
        administrator.setPassword(ADMIN_PASSWORD);
        administrator.setId(UUID.randomUUID().toString());
        administrator.setUpdated(new Date());
        users.put(ADMIN_USERNAME, administrator);
    };


    /**
     * <p>Timestamp at which the users list was last updated.</p>
     */
    public static Date usersUpdated = new Date();


}
