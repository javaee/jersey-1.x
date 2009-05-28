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

package com.sun.jersey.oauth.signature;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class SignatureTest extends TestCase {

    // values from OAuth specification appendices to demonstrate protocol operation
    private static final String REALM = "http://photos.example.net/";
    private static final String CONSUMER_KEY = "dpf43f3p2l4k3l03";
    private static final String ACCESS_TOKEN = "nnch734d00sl2jdk";
    private static final String SIGNATURE_METHOD = HMAC_SHA1.NAME;
    private static final String TIMESTAMP = "1191242096";
    private static final String NONCE = "kllo9940pd9333jh";
    private static final String VERSION = "1.0";
    private static final String SIGNATURE = "tR3+Ty81lMeYAr/Fid0kMTYa/WM=";

    /**
     * Creates the test case.
     *
     * @param testName name of the test case.
     */
    public SignatureTest(String testName) {
        super(testName);
    }

    /**
     * Returns the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(SignatureTest.class);
    }

    /**
     * Perform the test.
     */
    public void testSignature() {

        DummyRequest request = new DummyRequest().requestMethod("GET").
         requestURL("http://photos.example.net/photos").
         parameterValue("file", "vacation.jpg").parameterValue("size", "original");

        OAuthParameters params = new OAuthParameters().realm(REALM).
         consumerKey(CONSUMER_KEY).token(ACCESS_TOKEN).
         signatureMethod(SIGNATURE_METHOD).timestamp(TIMESTAMP).
         nonce(NONCE).version(VERSION);

        OAuthSecrets secrets = new OAuthSecrets().consumerSecret("kd94hf93k423kf44").
         tokenSecret("pfkkdhi9sl3r4s00");

        // generate digital signature; ensure it matches the OAuth spec
        String signature = null;

        try {
            signature = OAuthSignature.generate(request, params, secrets);
        }
        catch (OAuthSignatureException se) {
            fail(se.getMessage());
        }

        assertEquals(signature, SIGNATURE);

        OAuthParameters saved = (OAuthParameters)params.clone();

        try {
            // sign the request; clear params; parse params from request; ensure they match original
            OAuthSignature.sign(request, params, secrets);
        }
        catch (OAuthSignatureException se) {
            fail(se.getMessage());
        }

        // signing the request should not have modified the original parameters
        assertTrue(params.equals(saved));        
        assertTrue(params.getSignature() == null);

        params = new OAuthParameters();
        params.readRequest(request);
        assertEquals(params.getRealm(), REALM);
        assertEquals(params.getConsumerKey(), CONSUMER_KEY);
        assertEquals(params.getToken(), ACCESS_TOKEN);
        assertEquals(params.getSignatureMethod(), SIGNATURE_METHOD);
        assertEquals(params.getTimestamp(), TIMESTAMP);
        assertEquals(params.getNonce(), NONCE);
        assertEquals(params.getVersion(), VERSION);
        assertEquals(params.getSignature(), SIGNATURE);

        try {
            // verify signature using request that was just signed
            assertTrue(OAuthSignature.verify(request, params, secrets));
        }
        catch (OAuthSignatureException se) {
            fail(se.getMessage());
        }
    }
}
