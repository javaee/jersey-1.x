/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.samples.oauth.twitterclient;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Properties;

/** Simple command-line application that uses Jersey OAuth client library to authenticate
 * with Twitter.
 *
 * @author martin.matula@oracle.com
 */
public class App {
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));
    private static final String FRIENDS_TIMELINE_URI = "http://api.twitter.com/1/statuses/friends_timeline.xml";
    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTIES_FILE_NAME = "twitterclient.properties";
    private static final String PROPERTY_CONSUMER_KEY = "consumerKey";
    private static final String PROPERTY_CONSUMER_SECRET = "consumerSecret";
    private static final String PROPERTY_TOKEN = "token";
    private static final String PROPERTY_TOKEN_SECRET = "tokenSecret";

    public static void main(String[] args) throws Exception {
        // retrieve consumer key/secret and token/secret from the property file
        // or system properties
        loadSettings();

        // create a new Jersey client
        Client client = Client.create();

        // create a new auth handler
        OAuthClientFilter.AuthHandler authHandler = new OAuthClientFilter.AuthHandler() {
            @Override
            public void authorized(String token, String tokenSecret) {
                // we received an authorized access token - store it for future runs
                PROPERTIES.setProperty(PROPERTY_TOKEN, token);
                PROPERTIES.setProperty(PROPERTY_TOKEN_SECRET, tokenSecret);
            }

            @Override
            public String authorize(URI authorizationUri) {
                try {
                    // we don't have an authorized access token
                    // ask user to provide authorization and return the generated
                    // verifier code
                    System.out.println("Enter the following URI into a web browser and authorize me:");
                    System.out.println(authorizationUri);
                    System.out.print("Enter the authorization code: ");
                    return IN.readLine();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        // create a new OAuth client filter passing the needed info as well as the AuthHandler
        OAuthClientFilter filter = new OAuthClientFilter(
                client.getProviders(),
                new OAuthParameters().consumerKey(PROPERTIES.getProperty(PROPERTY_CONSUMER_KEY))
                    .token(PROPERTIES.getProperty(PROPERTY_TOKEN)),
                new OAuthSecrets().consumerSecret(PROPERTIES.getProperty(PROPERTY_CONSUMER_SECRET))
                    .tokenSecret(PROPERTIES.getProperty(PROPERTY_TOKEN_SECRET)),
                "http://twitter.com/oauth/request_token",
                "http://twitter.com/oauth/access_token",
                "http://twitter.com/oauth/authorize",
                authHandler);

        // Add filter to the client
        client.addFilter(filter);

        // make requests to protected resources
        // (no need to care about the authorization flow)
        do {
            List<Status> statuses = client.resource(FRIENDS_TIMELINE_URI)
                    .get(new GenericType<List<Status>>() {});
            for (Status s : statuses) {
                System.out.println(s.getText());
                System.out.println("[posted by " + s.getUser().getName() + " at " + s.getCreatedAt() + "]");
            }
            System.out.println();
            System.out.println("Please hit Enter to refresh, or enter a non-empty line to quit.");
        } while (IN.readLine().equals(""));

        // persist the current consumer key/secret and token/secret for future use
        storeSettings();
    }

    private static void loadSettings() {
        FileInputStream st = null;
        try {
            st = new FileInputStream(PROPERTIES_FILE_NAME);
            PROPERTIES.load(st);
        } catch (IOException e) {
            // ignore
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        for (String name : new String[] {PROPERTY_CONSUMER_KEY, PROPERTY_CONSUMER_SECRET,
                PROPERTY_TOKEN, PROPERTY_TOKEN_SECRET}) {
            String value = System.getProperty(name);
            if (value != null) {
                PROPERTIES.setProperty(name, value);
            }
        }

        if (PROPERTIES.getProperty(PROPERTY_CONSUMER_KEY) == null
                || PROPERTIES.getProperty(PROPERTY_CONSUMER_SECRET) == null) {
            System.out.println("No consumerKey and/or consumerSecret found in twitterclient.properties file. "
                    + "You have to provide these as system properties.");
            System.exit(1);
        }
    }

    private static void storeSettings() {
        FileOutputStream st = null;
        try {
            st = new FileOutputStream(PROPERTIES_FILE_NAME);
            PROPERTIES.store(st, null);
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                st.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}
