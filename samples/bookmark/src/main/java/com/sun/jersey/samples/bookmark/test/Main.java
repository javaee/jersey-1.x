/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.samples.bookmark.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author japod
 */
public class Main {

    final static String UrlBase = "http://localhost:8080/Bookmark/resources/";


    public static void main(String[] args) {
        try {
            // create a client:

            ClientConfig cc = new DefaultClientConfig();
            Client c = Client.create(cc);

            WebResource wr = c.resource(UrlBase);

            // get the initial representation
            
            System.out.println("Getting list of users:");
            JSONArray users = wr.path("users/").accept("application/json").get(JSONArray.class);
            System.out.println(String.format("List of users found:\n%s", users.toString()));
            System.out.println("-----");

            // add a new user
            
            System.out.println("Creating test user:");
            JSONObject user = new JSONObject();
            user.put("userid", "testuid").put("password", "test").put("email", "test@test.net").put("username", "Test User");
            wr.path("users/testuid").type("application/json").put(user);
            System.out.println("-----");

            // make sure it was added
            
            System.out.println("Getting list of users:");
            users = wr.path("users/").accept("application/json").get(JSONArray.class);
            System.out.println(String.format("List of users found:\n%s", users.toString()));
            System.out.println("-----");
            
            System.out.println("Getting test user before update:");
            user = wr.path("users/testuid").accept("application/json").get(JSONObject.class);
            System.out.println(String.format("User found:\n%s", user));
            System.out.println("-----");

            System.out.println("Updating test user:");
            user.put("password", "NEW PASSWORD").put("email", "NEW@EMAIL.NET").put("username", "UPDATED TEST USER");
            wr.path("users/testuid").type("application/json").put(user);
            System.out.println("-----");

            // make sure test user was updated

            System.out.println("Getting updated test user:");
            user = wr.path("users/testuid").accept("application/json").get(JSONObject.class);
            System.out.println(String.format("User found:\n%s", user));
            System.out.println("-----");
            
            WebResource bookmarksResource = c.resource(user.getString("bookmarks"));
           
            System.out.println("Getting list of test user bookmarks:");
            JSONArray bookmarks = bookmarksResource./*wr.path("users/testuid/bookmarks")*/accept("application/json").get(JSONArray.class);
            System.out.println(String.format("List of bookmarks found:\n%s", bookmarks));
            System.out.println("-----");

            System.out.println("Creating new bookmark for test user:");
            JSONObject bookmark = new JSONObject();
            bookmark.put("uri", "http://java.sun.com").put("sdesc", "test desc").put("ldesc", "long test description");
            bookmarksResource.type("application/json").post(bookmark);
            System.out.println("-----");

            System.out.println("Getting list of test user bookmarks after one was added:");
            bookmarks = wr.path("users/testuid/bookmarks").accept("application/json").get(JSONArray.class);
            System.out.println(String.format("List of bookmarks found:\n%s", bookmarks));
            System.out.println("-----");
            
            String testBookmarkUrl = bookmarks.getString(0);
            WebResource bookmarkResource = c.resource(testBookmarkUrl);
            
            System.out.println(String.format("Getting bookmark details, bookmark URL='%s':", testBookmarkUrl));
            bookmark = bookmarkResource.accept("application/json").get(JSONObject.class);
            System.out.println(String.format("Bookmark details:\n%s", bookmark));
            System.out.println("-----");

            System.out.println("Deleting test bookmark:");
            bookmarkResource.delete();
            System.out.println("-----");

            System.out.println("Getting list of test user bookmarks after test one was removed:");
            bookmarks = wr.path("users/testuid/bookmarks").accept("application/json").get(JSONArray.class);
            System.out.println(String.format("List of bookmarks found:\n%s", bookmarks));
            System.out.println("-----");

            System.out.println("Deleting test user:");
            wr.path("users/testuid").delete();
            System.out.println("-----");

            System.out.println("Getting list of users:");
            users = wr.path("users/").accept("application/json").get(JSONArray.class);
            System.out.println(String.format("List of users found:\n%s", users.toString()));
            System.out.println("-----");

        } catch (Exception e) {
            System.out.println("TEST FAILED! :-(");
            e.printStackTrace(System.out);
        }
    }
}
