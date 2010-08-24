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

package com.sun.jersey.oauth.server.spi;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;

/** Interface representing an OAuth token (i.e. access token or request token).
 *
 * @author Martin Matula
 */
public interface OAuthToken {
    /** Returns string representing the token.
     *
     * @return string representing the token
     */
    String getToken();

    /** Returns the token secret.
     *
     * @return token secret
     */
    String getSecret();

    /** Returns additional custom parameters associated with the token.
     * These will be included as additional parameters in the response to the
     * request token or access token request.
     *
     * @return immutable map of custom parameters
     */
    Map<String, List<String>> getCustomParameters();

    /** Returns a {@link Principal} object containing the name of the
     * user the request containing this token is authorized to act on behalf of.
     * When the oauth filter verifies the request
     * with this token is properly authenticated, it injects this token into a security context
     * which then delegates {@link SecurityContext#getUserPrincipal()} to this
     * method.
     *
     * @return Principal corresponding to this token, or null if the token is not authorized
     */
    Principal getPrincipal();

    /** Returns a boolean indicating whether this token is authorized for the
     * specified logical "role". When the oauth filter verifies the request
     * with this token is properly authenticated, it injects this token into a security context
     * which then delegates {@link SecurityContext#isUserInRole(java.lang.String)} to this
     * method.
     *
     * @param role a {@code String} specifying the name of the role
     *
     * @return a {@code boolean} indicating whether this token is authorized for
     * a given role
     */
    boolean isInRole(String role);
}
