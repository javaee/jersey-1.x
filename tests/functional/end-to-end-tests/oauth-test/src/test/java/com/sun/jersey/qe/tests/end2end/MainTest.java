/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.qe.tests.end2end;

import java.util.jar.Manifest;

import com.sun.jersey.oauth.signature.HMAC_SHA1;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import com.sun.jersey.oauth.signature.RSA_SHA1;
import com.sun.jersey.spi.service.ServiceFinder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * These tests were copied from {@code jersey-oauth} module. If you're changing them, consider changing tests in the mentioned
 * module as well.
 *
 * @author Paul C. Bryan
 * @author Hubert A. Le Van Gong
 */
public class MainTest {

    // values from OAuth specification appendices to demonstrate protocol operation
    private static final String REALM = "http://photos.example.net/";
    private static final String CONSUMER_KEY = "dpf43f3p2l4k3l03";
    private static final String ACCESS_TOKEN = "nnch734d00sl2jdk";
    private static final String SIGNATURE_METHOD = HMAC_SHA1.NAME;
    private static final String TIMESTAMP = "1191242096";
    private static final String NONCE = "kllo9940pd9333jh";
    private static final String VERSION = "1.0";
    private static final String SIGNATURE = "tR3+Ty81lMeYAr/Fid0kMTYa/WM=";

    private static final String RSA_PRIVKEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALRiMLAh9iimur8V" +
                    "A7qVvdqxevEuUkW4K+2KdMXmnQbG9Aa7k7eBjK1S+0LYmVjPKlJGNXHDGuy5Fw/d" +
                    "7rjVJ0BLB+ubPK8iA/Tw3hLQgXMRRGRXXCn8ikfuQfjUS1uZSatdLB81mydBETlJ" +
                    "hI6GH4twrbDJCR2Bwy/XWXgqgGRzAgMBAAECgYBYWVtleUzavkbrPjy0T5FMou8H" +
                    "X9u2AC2ry8vD/l7cqedtwMPp9k7TubgNFo+NGvKsl2ynyprOZR1xjQ7WgrgVB+mm" +
                    "uScOM/5HVceFuGRDhYTCObE+y1kxRloNYXnx3ei1zbeYLPCHdhxRYW7T0qcynNmw" +
                    "rn05/KO2RLjgQNalsQJBANeA3Q4Nugqy4QBUCEC09SqylT2K9FrrItqL2QKc9v0Z" +
                    "zO2uwllCbg0dwpVuYPYXYvikNHHg+aCWF+VXsb9rpPsCQQDWR9TT4ORdzoj+Nccn" +
                    "qkMsDmzt0EfNaAOwHOmVJ2RVBspPcxt5iN4HI7HNeG6U5YsFBb+/GZbgfBT3kpNG" +
                    "WPTpAkBI+gFhjfJvRw38n3g/+UeAkwMI2TJQS4n8+hid0uus3/zOjDySH3XHCUno" +
                    "cn1xOJAyZODBo47E+67R4jV1/gzbAkEAklJaspRPXP877NssM5nAZMU0/O/NGCZ+" +
                    "3jPgDUno6WbJn5cqm8MqWhW1xGkImgRk+fkDBquiq4gPiT898jusgQJAd5Zrr6Q8" +
                    "AO/0isr/3aa6O6NLQxISLKcPDk2NOccAfS/xOtfOz4sJYM3+Bs4Io9+dZGSDCA54" +
                    "Lw03eHTNQghS0A==";
    private static final String RSA_SIGNATURE_METHOD = RSA_SHA1.NAME;
    private static final String RSA_SIGNATURE = "jvTp/wX1TYtByB1m+Pbyo0lnCOLI" +
            "syGCH7wke8AUs3BpnwZJtAuEJkvQL2/9n4s5wUmUl4aCI4BwpraNx4RtEXMe5qg5" +
            "T1LVTGliMRpKasKsW//e+RinhejgCuzoH26dyF8iY2ZZ/5D1ilgeijhV/vBka5tw" +
            "t399mXwaYdCwFYE=";

    private static final String RSA_NONCE = "13917289812797014437";
    private static final String RSA_TIMESTAMP = "1196666512";

    @Test
    public void testHMAC_SHA1() {
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
        } catch (OAuthSignatureException se) {
            fail(se.getMessage());
        }

        assertEquals(signature, SIGNATURE);
    }

    @Test
    public void testRSASHA1() {
        DummyRequest request = new DummyRequest().requestMethod("GET").
                requestURL("http://photos.example.net/photos").
                parameterValue("file", "vacaction.jpg").parameterValue("size", "original");

        OAuthParameters params = new OAuthParameters().realm(REALM).
                consumerKey(CONSUMER_KEY).
                signatureMethod(RSA_SIGNATURE_METHOD).timestamp(RSA_TIMESTAMP).
                nonce(RSA_NONCE).version(VERSION);

        OAuthSecrets secrets = new OAuthSecrets().consumerSecret(RSA_PRIVKEY);

        // generate digital signature; ensure it matches the OAuth spec
        String signature = null;

        try {
            signature = OAuthSignature.generate(request, params, secrets);
        }
        catch (OAuthSignatureException se) {
            fail(se.getMessage());
        }
        assertEquals(signature, RSA_SIGNATURE);
    }

    @Test
    public void testOAuthSignatureMethod() {
        final ServiceFinder<?> methods = ServiceFinder.find("com.sun.jersey.oauth.signature.OAuthSignatureMethod");
        assertTrue(methods.toArray().length > 0);
    }

    @Test
    public void testManifestBundleVersion() throws Exception {
        final Manifest manifestOAuth = Helper.getManifest(OAuthSignature.class);
        final Manifest manifestServiceFinder = Helper.getManifest(ServiceFinder.class);

        assertNotSame(manifestServiceFinder.getMainAttributes().getValue("Implementation-Version"),
                manifestOAuth.getMainAttributes().getValue("Implementation-Version"));
    }

}
