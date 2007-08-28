/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */
package com.sun.ws.rest.api.core;

/**
 * Utility class for validating, encoding and decoding components
 * of a URI.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class UriComponent {

    public enum Type {
        SCHEME, USER_INFO, HOST, PATH, QUERY, FRAGMENT,
    }
    
    private UriComponent() {
    }

    /**
     * Validates the characters of a percent-encoded string that represents a 
     * URI component type.
     *
     * @param s the encoded string.
     * @param t the URI compontent type identifying the ASCII characters that 
     *          must be percent-encoded.
     * @return true if the encoded string is valid.
     */
    public boolean validate(String s, Type t) {
        throw new UnsupportedOperationException();
    }

    /**
     * Encodes the characters of string that are either non-ASCII characters 
     * or are ASCII characters that must be percent-encoded using the 
     * UTF-8 encoding.
     *
     * @param s the string to be encoded.
     * @param t the URI compontent type identifying the ASCII characters that 
     *          must be percent-encoded.
     * @return the encoded string.
     */
    public String encode(String s, Type t) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Decodes characters of a string that are percent-encoded octets using 
     * UTF-8 decoding (if needed).
     * <p>
     * It is assumed that the string is valid according to an (unspecified) URI 
     * component type. If a sequence of contiguous percent-encoded octets is 
     * not a valid UTF-8 character then the octets are replaced with '\uFFFD'.
     * <p>
     * Any "%" found between "[]" is left alone. It is an IPv6 literal with a 
     * scope_id.
     * <p>
     * @param s the string to be decoded.
     * @return the decoded string.
     */
    public String decode(String s) {
        throw new UnsupportedOperationException();
    }
}