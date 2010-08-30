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

package com.sun.jersey.oauth.server.api;

import com.sun.jersey.oauth.server.spi.OAuthToken;
import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.spi.container.ContainerRequest;
import java.util.HashSet;
import java.util.regex.Pattern;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.oauth.server.NonceManager;
import com.sun.jersey.oauth.server.OAuthException;
import com.sun.jersey.oauth.server.OAuthSecurityContext;
import com.sun.jersey.oauth.server.OAuthServerRequest;
import com.sun.jersey.oauth.server.spi.OAuthConsumer;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.util.Collections;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/** OAuth request filter that filters all requests indicating in the Authorization
 * header they use OAuth. Checks if the incoming requests are properly authenticated
 * and populates the security context with the corresponding user principal and roles.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey filter can be registered using the following initialization parameters:
 * <pre>
 * &lt;init-param&gt;
 *     &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *     &lt;param-value&gt;com.sun.jersey.oauth.server.api.OAuthServerFilter&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 *
 * <p>
 * This filter requires an implementation of {@link OAuthProvider} interface to be
 * registered through the {@link OAuthServerFilter#PROPERTY_PROVIDER} property.
 * <p>
 * The constants in this class indicate how you can parameterize this filter. E.g. when an application
 * is deployed as a Servlet or Filter you can set the path patern to be ignored by this filter
 * using the following initialization parameter:
 * <pre>
 * &lt;init-param&gt;
 *     &lt;param-name&gt;com.sun.jersey.config.property.oauth.ignorePathPattern&lt;/param-name&gt;
 *     &lt;param-value&gt;/login&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 * @author Martin Matula
 */
public class OAuthServerFilter implements ContainerRequestFilter {
    /** Mandatory property - class name of the OAuthProvider class. */
    public static final String PROPERTY_PROVIDER = "com.sun.jersey.config.property.oauth.provider";
    /** OAuth realm. Default is set to "default". */
    public static final String PROPERTY_REALM = "com.sun.jersey.config.property.oauth.realm";
    /** Property that can be set to a regular expression used to match the path (relative to the base URI) this
     * filter should not be applied to. */
    public static final String PROPERTY_IGNORE_PATH_PATTERN = "com.sun.jersey.config.property.oauth.ignorePathPattern";
    /** Can be set to max. age (in milliseconds) of nonces that should be tracked (default = 300000 ms = 5 min). */
    public static final String PROPERTY_MAX_AGE = "com.sun.jersey.config.property.oauth.maxAge";
    /** Property that can be set to frequency of collecting nonces exceeding max. age (default = 100 = every 100 requests). */
    public static final String PROPERTY_GC_PERIOD = "com.sun.jersey.config.property.oauth.gcPeriod";
    /** If set to true makes the correct OAuth authentication optional - i.e. instead of returning the appropriate status code
     * ({@link Response.Status#BAD_REQUEST} or {@link Response.Status#UNAUTHORIZED}) the filter
     * will ignore this request (as if it was not authenticated) and let the web application deal with it. */
    public static final String FEATURE_NO_FAIL = "com.sun.jersey.config.feature.oauth.noFail";

    /** OAuth Server */
    private final OAuthProvider provider;

    /** Manages and validates incoming nonces. */
    private final NonceManager nonces;

    /** Maximum age (in milliseconds) of timestamp to accept in incoming messages. */
    private final int maxAge;

    /** Average requests to process between nonce garbage collection passes. */
    private final int gcPeriod;

    /** Value to return in www-authenticate header when 401 response returned. */
    private final String wwwAuthenticateHeader;

    /** OAuth protocol versions that are supported. */
    private final Set<String> versions;

    /** Regular expression pattern for path to ignore. */
    private final Pattern ignorePathPattern;

    private final boolean optional;

    public OAuthServerFilter(@Context ResourceConfig rc, @Context OAuthProvider provider) {
        this.provider = provider;

        // establish supported OAuth protocol versions
        HashSet<String> v = new HashSet<String>();
        v.add(null);
        v.add("1.0");
        versions = Collections.unmodifiableSet(v);

        // optional initialization parameters (defaulted)
        String realm = defaultInitParam(rc, PROPERTY_REALM, "default");
        maxAge = intValue(defaultInitParam(rc, PROPERTY_MAX_AGE, "300000")); // 5 minutes
        gcPeriod = intValue(defaultInitParam(rc, PROPERTY_GC_PERIOD, "100")); // every 100 on average
        ignorePathPattern = pattern(defaultInitParam(rc, PROPERTY_IGNORE_PATH_PATTERN, null)); // no pattern
        optional = rc.getFeature(FEATURE_NO_FAIL);

        nonces = new NonceManager(maxAge, gcPeriod);

        // www-authenticate header for the life of the object
        wwwAuthenticateHeader = "OAuth realm=\"" + realm + "\"";
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        // do not filter requests that do not use OAuth authentication
        String authHeader = request.getHeaderValue(OAuthParameters.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.toUpperCase().startsWith(OAuthParameters.SCHEME.toUpperCase())) {
            return request;
        }

        // do not filter if the request path matches pattern to ignore
        if (match(ignorePathPattern, request.getPath())) {
            return request;
        }

        OAuthSecurityContext sc = null;

        try {
            sc = getSecurityContext(request);
        } catch (OAuthException e) {
            if (optional) {
                return request;
            } else {
                throw new WebApplicationException(e.toResponse());
            }
        }

        request.setSecurityContext(sc);
        return request;
    }

    private OAuthSecurityContext getSecurityContext(ContainerRequest request) throws OAuthException {
        OAuthServerRequest osr = new OAuthServerRequest(request);
        OAuthParameters params = new OAuthParameters().readRequest(osr);

        // apparently not signed with any OAuth parameters; unauthorized
        if (params.size() == 0) {
            throw newUnauthorizedException();
        }

        // get required OAuth parameters
        String consumerKey = requiredOAuthParam(params.getConsumerKey());
        String token = requiredOAuthParam(params.getToken());
        String timestamp = requiredOAuthParam(params.getTimestamp());
        String nonce = requiredOAuthParam(params.getNonce());

        // enforce other supported and required OAuth parameters
        requiredOAuthParam(params.getSignature());
        supportedOAuthParam(params.getVersion(), versions);

        // retrieve secret for consumer key
        OAuthConsumer consumer = provider.getConsumer(consumerKey);
        if (consumer == null) {
            throw newUnauthorizedException();
        }

        OAuthToken accessToken = provider.getAccessToken(token);
        if (accessToken == null) {
            throw newUnauthorizedException();
        }

        OAuthConsumer atConsumer = accessToken.getConsumer();
        if (atConsumer == null || !atConsumer.getSecret().equals(consumer.getSecret())) {
            throw newUnauthorizedException();
        }

        OAuthSecrets secrets = new OAuthSecrets().consumerSecret(consumer.getSecret()).tokenSecret(accessToken.getSecret());

        if (!verifySignature(osr, params, secrets)) {
            throw newUnauthorizedException();
        }

        if (!nonces.verify(token, timestamp, nonce)) {
            throw newUnauthorizedException();
        }

        return new OAuthSecurityContext(accessToken, request.isSecure());
    }

    private static String defaultInitParam(ResourceConfig config, String name, String value) {
        String v = (String) config.getProperty(name);
        if (v == null || v.length() == 0) {
            v = value;
        }
        return v;
    }

    private static int intValue(String value) {
        try {
            return Integer.valueOf(value);
        }
        catch (NumberFormatException nfe) {
           return -1;
        }
    }

    private static String requiredOAuthParam(String value) throws OAuthException {
        if (value == null) {
            throw newBadRequestException();
        }
        return value;
    }

    private static String supportedOAuthParam(String value, Set<String> set) throws OAuthException {
        if (!set.contains(value)) {
            throw newBadRequestException();
        }
        return value;
    }

    private static Pattern pattern(String p) {
        if (p == null) {
            return null;
        }
        return Pattern.compile(p);
    }

    private static boolean match(Pattern pattern, String value) {
        return (pattern != null && value != null && pattern.matcher(value).matches());
    }

    private static boolean verifySignature(OAuthServerRequest osr,
    OAuthParameters params, OAuthSecrets secrets) {
        try {
            return OAuthSignature.verify(osr, params, secrets);
        } catch (OAuthSignatureException ose) {
            throw newBadRequestException();
        }
    }

    private static OAuthException newBadRequestException() throws OAuthException {
        return new OAuthException(Response.Status.BAD_REQUEST, null);
    }

    private OAuthException newUnauthorizedException() throws OAuthException {
        return new OAuthException(Response.Status.UNAUTHORIZED, wwwAuthenticateHeader);
    }
}
