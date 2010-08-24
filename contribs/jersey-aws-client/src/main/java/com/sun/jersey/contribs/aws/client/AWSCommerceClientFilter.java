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

package com.sun.jersey.contribs.aws.client;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.util.Base64;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * The filter allows you to make AWS Product Advertising API (former AWS Commerce API) calls
 * without a need to sign every single request programmatically.
 * The filter will automatically check the request query and update it as needed.
 * It will namely try to add the Timestamp and AWSAccessKeyId parameters if missing
 * and add or replace Signature parameter with a valid signature of the request.
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public final class AWSCommerceClientFilter extends ClientFilter {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static final String HashAlgorithm = "HmacSHA256";
    
    static final String TimestampName = "Timestamp";
    static final String AWSAccessKeyIdName = "AWSAccessKeyId";
    static final String SignatureName = "Signature";

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    final String awsKeyId;
    final String awsSecretAccessKey;
    final Mac mac;


    private final class AWSQuery {

        final String query;

        AWSQuery(ClientRequest request) {

            final MultivaluedMap<String, String> queryParams = extractQueryParams(request);
            removeSignature(queryParams);
            addTimestamp(queryParams);
            addAccessKeyId(queryParams);

            query = getQuerySortedByParamNames(queryParams);
        }


        private MultivaluedMap<String, String> extractQueryParams(ClientRequest request) {
            return UriComponent.decodeQuery(request.getURI(), true);
        }

        private void removeSignature(final MultivaluedMap<String, String> queryParams) {
            if (queryParams.containsKey(SignatureName)) {
                queryParams.remove(SignatureName);
            }
        }

        private void addTimestamp(final MultivaluedMap<String, String> queryParams) {
            if (!queryParams.containsKey(TimestampName)) {
                queryParams.add(TimestampName, dateFormat.format(new Date()));
            }
        }

        private void addAccessKeyId(final MultivaluedMap<String, String> queryParams) {
            if (!queryParams.containsKey(AWSAccessKeyIdName)) {
                queryParams.add(AWSAccessKeyIdName, awsKeyId);
            }
        }

        String getQuerySortedByParamNames(final MultivaluedMap<String, String> queryParams) {

            Set<String> sortedParameterNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            sortedParameterNames.addAll(queryParams.keySet());

            StringBuilder result = new StringBuilder();

            boolean firstParam = true;

            for (String key : sortedParameterNames) {
                final List<String> values = queryParams.get(key);
                for (String val : values) {
                    String nextParamKeyValuePair = String.format("%s=%s", key, encodeParam(val));
                    if (firstParam) {
                        firstParam = false;
                    } else {
                        result.append('&');
                    }
                    result.append(nextParamKeyValuePair);
                }
            }

            return result.toString();
        }


        private String encodeParam(String val) {
            String result = UriComponent.encode(val, UriComponent.Type.QUERY);
            return result.replaceAll(":", "%3A");
        }

    }

    /**
     *
     * Creates a new Amazon Product Advertising API client filter instance.
     * You will need valid pair of AWS access keys (Access Key and Secret Access Key).
     * The keys should be accessible from http://aws.amazon.com/security-credentials
     * An exception will be thrown if you provide an invalid secret access key
     * or if the needed security HASH algorithm implementation is missing at your platform.
     *
     * @param awsKeyId AWS Access Key
     * @param awsSecretAccessKey AWS Secure Access Key
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public AWSCommerceClientFilter(String awsKeyId, String awsSecretAccessKey) throws InvalidKeyException, NoSuchAlgorithmException {
        this.awsKeyId = awsKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.mac = Mac.getInstance(HashAlgorithm);
        this.mac.init(new SecretKeySpec(awsSecretAccessKey.getBytes(), HashAlgorithm));
    }

    @Override
    public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {
        return getNext().handle(getSignedRequest(request));
    }


    private ClientRequest getSignedRequest(final ClientRequest request) {

        final AWSQuery awsQuery = new AWSQuery(request);

        final String toSign = getStringToSign(request, awsQuery);
        final String signature = calculateSignature(toSign);

        final ClientRequest result = request.clone();
        result.setURI(UriBuilder.fromUri(request.getURI()).replaceQuery(appendSignature(awsQuery.query, signature)).build());

        return result;
    }

    private String getStringToSign(final ClientRequest request, final AWSQuery awsQueryParams) {

        StringBuilder result = new StringBuilder();

        result.append(request.getMethod());
        result.append('\n');
        result.append(request.getURI().getHost());
        result.append('\n');
        result.append(request.getURI().getPath());
        result.append('\n');
        result.append(awsQueryParams.query);

        return result.toString();
    }

    private String calculateSignature(final String toSign) throws ClientHandlerException {
        try {
            return new String(Base64.encode(mac.doFinal(toSign.getBytes("UTF-8"))));
        } catch (Exception ex) {
            throw new ClientHandlerException(ex);
        }
    }

    private String appendSignature(final String query, final String signature) {
        final StringBuilder result = new StringBuilder(query);
        result.append('&').append(SignatureName).append('=').append(UriComponent.encode(signature, UriComponent.Type.QUERY_PARAM));
        return result.toString();
    }
}
