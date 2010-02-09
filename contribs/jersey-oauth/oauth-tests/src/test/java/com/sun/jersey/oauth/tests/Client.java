/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.oauth.tests;

import javax.ws.rs.ext.Providers;
import junit.framework.Assert;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthParameters;

class Client extends Assert {

    /**
     * Executes the client tests against the running server.
     */
    public static void execute(String host, int port) {
   
        String base = "http://" + host + ":" + port;

        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
        client.addFilter(new LoggingFilter());
        Providers providers = client.getProviders();

        // baseline for requests
        OAuthSecrets secrets = new OAuthSecrets().consumerSecret("kd94hf93k423kf44");
        OAuthParameters params = new OAuthParameters().consumerKey("dpf43f3p2l4k3l03").
         signatureMethod("PLAINTEXT").version("1.0");
        OAuthClientFilter filter = new OAuthClientFilter(providers, params, secrets);
        WebResource resource;
        String response;

        params.timestamp("1191242090").nonce("hsu94j3884jdopsl");
        resource = client.resource(base + "/request_token");
        resource.addFilter(filter);
        response = resource.post(String.class);
        assertEquals(response, "oauth_token=hh5s93j4hdidpola&oauth_token_secret=hdhd0244k9j7ao03");

        secrets.tokenSecret("hdhd0244k9j7ao03");
        params.token("hh5s93j4hdidpola").timestamp("1191242092").nonce("dji430splmx33448");
        resource = client.resource(base + "/access_token");
        resource.addFilter(filter);
        response = resource.post(String.class);
        assertEquals(response, "oauth_token=nnch734d00sl2jdk&oauth_token_secret=pfkkdhi9sl3r4s00");

        secrets.tokenSecret("pfkkdhi9sl3r4s00");
        params.token("nnch734d00sl2jdk").signatureMethod("HMAC-SHA1").timestamp("1191242096").
         nonce("kllo9940pd9333jh");
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("file", "vacation.jpg");
        queryParams.add("size", "original");
        resource = client.resource(base + "/photos").queryParams(queryParams);
        resource.addFilter(filter);
        response = resource.get(String.class);
        assertEquals(response, "PHOTO");
    }
}

