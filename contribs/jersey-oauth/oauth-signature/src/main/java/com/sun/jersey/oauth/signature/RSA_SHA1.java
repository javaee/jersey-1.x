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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * An OAuth signature method that implements RSA-SHA1.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class RSA_SHA1 implements OAuthSignatureMethod {

    public static final String NAME = "RSA-SHA1";

    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    
    private static final String KEY_TYPE = "RSA";

    public RSA_SHA1() {
    }

    public String name() {
        return NAME;
    }

    /**
     * Generates the RSA-SHA1 signature of OAuth request elements.
     *
     * @param elements the combined OAuth elements to sign.
     * @param secrets the secrets object containing the private key for generating the signature.
     * @return the OAuth signature, in base64-encoded form.
     * @throws InvalidSecretException if the supplied secret is not valid.
     */
    public String sign(String elements, OAuthSecrets secrets) throws InvalidSecretException {
    
        Signature sig;
        try { sig = Signature.getInstance(SIGNATURE_ALGORITHM); }
        catch (NoSuchAlgorithmException nsae) { throw new IllegalStateException(nsae); }

        byte[] decodedPrivKey;
        try { decodedPrivKey = Base64.decode(secrets.getConsumerSecret()); }
        catch (IOException ioe) { throw new InvalidSecretException("invalid consumer secret"); }

        KeyFactory keyf;
        try { keyf = KeyFactory.getInstance(KEY_TYPE); }
        catch (NoSuchAlgorithmException nsae) { throw new IllegalStateException(nsae); }

        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivKey);
        
        RSAPrivateKey rsaPrivKey;
        try { rsaPrivKey = (RSAPrivateKey) keyf.generatePrivate(keySpec); }
        catch (InvalidKeySpecException ikse) { throw new IllegalStateException(ikse); }

        try { sig.initSign(rsaPrivKey); }
        catch (InvalidKeyException ike) { throw new IllegalStateException(ike); }

        try { sig.update(elements.getBytes()); }
        catch (SignatureException se) { throw new IllegalStateException(se); }

        byte[] rsasha1;
        try { rsasha1 = sig.sign(); }
        catch (SignatureException se) { throw new IllegalStateException(se); }

        return new String(Base64.encode(rsasha1));
    }

    /**
     * Verifies the RSA-SHA1 signature of OAuth request elements.
     *
     * @param elements OAuth elements signature is to be verified against.
     * @param secrets the secrets object containing the public key for verifying the signature.
     * @param signature base64-encoded OAuth signature to be verified.
     * @throws InvalidSecretException if the supplied secret is not valid.
     */
    public boolean verify(String elements, OAuthSecrets secrets, String signature) throws InvalidSecretException {

        Signature sig;
        try { sig = Signature.getInstance(SIGNATURE_ALGORITHM); }
        catch (NoSuchAlgorithmException nsae) { throw new IllegalStateException(nsae); }

        byte[] decodedPubKey;
        try { decodedPubKey = Base64.decode(secrets.getConsumerSecret()); }
        catch (IOException ioe) { throw new InvalidSecretException("invalid consumer secret"); }

        byte[] decodedSignature;
        try { decodedSignature = Base64.decode(signature); }
        catch (IOException ioe) { return false; }

        KeyFactory keyf;
        try { keyf = KeyFactory.getInstance(KEY_TYPE); }
        catch (NoSuchAlgorithmException nsae) { throw new IllegalStateException(nsae); }

        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPubKey);

        RSAPublicKey rsaPubKey;
        try { rsaPubKey = (RSAPublicKey)keyf.generatePublic(keySpec); }
        catch (InvalidKeySpecException ikse) { throw new IllegalStateException(ikse); }

        try { sig.initVerify(rsaPubKey); }
        catch (InvalidKeyException ike) { throw new IllegalStateException(ike); }

        try { sig.update(elements.getBytes()); }
        catch (SignatureException se) { throw new IllegalStateException(se); }
        
        try { return sig.verify(decodedSignature); }
        catch (SignatureException se) { throw new IllegalStateException(se); }
    }
}
