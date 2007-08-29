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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

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
    public static boolean validate(String s, Type t) {
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
    public static String encode(String s, Type t) {
        throw new UnsupportedOperationException();
    }

    
    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    
    /**
     * Decodes characters of a string that are percent-encoded octets using 
     * UTF-8 decoding (if needed).
     * <p>
     * It is assumed that the string is valid according to an (unspecified) URI 
     * component type. If a sequence of contiguous percent-encoded octets is 
     * not a valid UTF-8 character then the octets are replaced with '\uFFFD'.
     * <p>
     * If the URI component is of type HOST then any "%" found between "[]" is 
     * left alone. It is an IPv6 literal with a scope_id.
     * <p>
     * @param s the string to be decoded.
     * @param t the URI component type, may be null.
     * @return the decoded string.
     */
    public static String decode(String s, Type t) {
	if (s == null)
	    throw new IllegalArgumentException();
        
	final int n = s.length();
	if (n == 0)
	    return s;

        // If there are no percent-escaped octets
	if (s.indexOf('%') < 0)
	    return s;
        
        // Malformed percent-escaped octet at the end
        if (n < 2)
	    throw new IllegalArgumentException();
            
        // Malformed percent-escaped octet at the end
        if (s.charAt(n - 2) == '%')
	    throw new IllegalArgumentException();

        return (t != Type.HOST) ? decode(s, n) : decodeHost(s, n);
    }
        
    private static String decode(String s, int n) {
	final StringBuilder sb = new StringBuilder(n);
	ByteBuffer bb = ByteBuffer.allocate(1);

	for (int i = 0; i < n;) {
            final char c = s.charAt(i++);
	    if (c != '%') {
		sb.append(c);
	    } else {            
                bb = decodePercentEncodedOctets(s, i, bb);
                i = decodeOctets(i, bb, sb);
            }
	}

	return sb.toString();
    }
    
    private static String decodeHost(String s, int n) {
	final StringBuilder sb = new StringBuilder(n);
	ByteBuffer bb = ByteBuffer.allocate(1);

    	boolean betweenBrackets = false;
	for (int i = 0; i < n;) {
            final char c = s.charAt(i++);
	    if (c == '[') {
		betweenBrackets = true;
	    } else if (betweenBrackets && c == ']') {
		betweenBrackets = false;
	    }
            
	    if (c != '%' || betweenBrackets) {
		sb.append(c);
	    } else {
                bb = decodePercentEncodedOctets(s, i, bb);
                i = decodeOctets(i, bb, sb);
            }
	}

	return sb.toString();        
    }
    
    /**
     * Decode a contigious sequence of percent encoded octets.
     * <p>
     * Assumes the index, i, starts that the first hex digit of the first
     * percent-encoded octet.
     */
    private static ByteBuffer decodePercentEncodedOctets(String s, int i, ByteBuffer bb) {
        bb.clear();
        
        while (true) {
            // Decode the hex digits
            bb.put((byte) (decodeHex(s, i++) << 4 | decodeHex(s, i++)));

            // Finish if at the end of the string
            if (i == s.length())
                break;

            // Finish if no more percent-encoded octets follow
            if (s.charAt(i++) != '%')
                break;
            
            // Check of the byte buffer needs to be increased in size
            if (bb.position() == bb.capacity()) {
                bb.flip();
                // Create a new byte buffer with the maximum number of possible
                // octets, hence resize should only occur once
                ByteBuffer bb_new = ByteBuffer.allocate(s.length() / 3);
                bb_new.put(bb);
                bb = bb_new;
            }
        }
        
        bb.flip();
        return bb;
    }
    
    /**
     * Decodes octets to characters using the UTF-8 decoding and appends
     * the characters to a StringBuffer.
     * @return the index to the next unchecked character in the string to decode
     */
    private static int decodeOctets(int i, ByteBuffer bb, StringBuilder sb) {
        // If there is only one octet and is an ASCII character
        if (bb.limit() == 1 && (bb.get(0) & 0xFF) < 128) {
            // Octet can be appended directly
            sb.append((char)bb.get(0));
            return i + 2;
        } else {
            // 
            CharBuffer cb = UTF_8_CHARSET.decode(bb);
            sb.append(cb.toString());
            return i + bb.limit() * 3 - 1;
        }
    }
    
    private static int decodeHex(String s, int i) {
        final int v = decodeHex(s.charAt(i));
        if (v == -1)
            throw new IllegalArgumentException("Malformed hex character '" + s.charAt(i) + "' at index " + i);
        return v;
    }    
    
    private static final int[] HEX_TABLE = createHexTable();
    
    private static int[] createHexTable() {
        int[] table = new int[128];
        Arrays.fill(table, -1);
        
        for (char c = '0'; c <= '9'; c++) table[c] = c - '0';
        for (char c = 'A'; c <= 'F'; c++) table[c] = c - 'A' + 10;
        for (char c = 'a'; c <= 'f'; c++) table[c] = c - 'a' + 10;
        return table;
    }

    private static int decodeHex(char c) {
        return (c < 128) ? HEX_TABLE[c] : -1;
    }    
}