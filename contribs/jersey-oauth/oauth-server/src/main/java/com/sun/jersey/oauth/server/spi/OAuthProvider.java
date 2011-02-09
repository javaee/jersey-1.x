/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.oauth.server.spi;

import java.util.List;
import java.util.Map;

/** Contract for a provider that supports managing OAuth tokens and consumer secrets.
 * To add an {@code OAuthProvider} implementation, annotate the implementation
 * class with {@code &#64;Provider}.
 *
 * @author Martin Matula
 */
public interface OAuthProvider {

    /** Gets consumer corresponding to a given consumer key.
     *
     * @param consumerKey consumer key
     * @return corresponding consumer secret or {@literal null} if no consumer with the given key is known
     */
    OAuthConsumer getConsumer(String consumerKey);

    /** Creates a new request token for a given consumerKey.
     *
     * @param consumerKey consumer key to create a request token for
     * @param callbackUrl callback url for this request token request
     * @param attributes additional service provider-specific parameters
     *      (this can be used to indicate what level of access is requested
     *      - i.e. readonly, or r/w, etc.)
     * @return new request token
     */
    OAuthToken newRequestToken(String consumerKey, String callbackUrl, Map<String, List<String>> attributes);

    /** Returns the request token by the consumer key and token value.
     *
     * @param token request token value
     * @return request token or {@literal null} if no such token corresponding to a given
     * consumer key is found
     */
    OAuthToken getRequestToken(String token);

    /** Creates a new access token. This method must validate the passed arguments
     * and return {@literal null} if any of them is invalid.
     *
     * @param requestToken authorized request token
     * @param verifier verifier passed to the callback after authorization
     * @return new access token or null if the arguments are invalid (e.g. there
     * is no such request token as in the argument, or the verifier does not match)
     */
    OAuthToken newAccessToken(OAuthToken requestToken, String verifier);

    /** Returns the access token by the consumer key and token value.
     *
     * @param token access token value
     * @return access token or {@literal null} if no such found
     */
    OAuthToken getAccessToken(String token);
}
