/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.api.multipart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * <p>A robust implementation of creating and parsing an HTTP header value,
 * including support for name/value pairs that are appended.  Two primary
 * use cases are envisioned -- building up a header value to be output to
 * an HTTP request or response, and parsing an existing header value received
 * as part of an HTTP request or response.</p>
 *
 * <p>To build up a header value scratch, two approaches may be used.  First,
 * you might specify a String that already contains parameters to the
 * constructor (or as an argument to <code>setValue()</code>.  Examples:</p>
 *
 * <pre>
 * HeaderValue headerValue = new HeaderValue("foo"); // No parameters included
 *
 * HeaderValue headerValue = new HeaderValue("form-data; name=\"field1\""); // Include a parameter
 * </pre>
 *
 * <p>Alternatively, you can start with just the value, and append parameter
 * name/value pairs separately.</p>
 * <pre>
 *   HeaderValue headerValue = new HeaderValue("form-data");
 *   headerValue.getParameters().put("name", "field1");
 * </pre>
 *
 * <p>When you receive an HTTP header in a request or response, you can also
 * use this class to parse the values and parameters.  Assume that variable
 * <code>received</code> in the example below was parsed from an incoming
 * header (rather than being hard coded).  Then, you can extract information:</p>
 *
 * <pre>
 *   String received = "form-data; name=\"field1\""; // Would really be parsed from a request or response
 *
 *   HeaderValue headerValue = new HeaderValue(received);
 *   System.out.println(headerValue.getValue()); // Prints "form-data"
 *   System.out.println(headerValue.getParameters().get("name")); // Prints "field1"
 * </pre>
 *
 * <p>If you are parsing received headers, you may not know ahead of time whether
 * multiple values have been supplied (separated by commas).  For convenience,
 * you can deal with this situation by calling <code>parseHeaderValues()</code>:</p>
 *
 * <pre>
 *   String received = ...; // Value(s) string parsed from the header
 *   List<HeaderValue> headerValues = HeaderValue.parseHeaderValues(received);
 * </pre>
 *
 * <p>FIXME - Current parsing algorithms do not support nesting commas or
 * semicolons inside a double-quoted string.  It is not (yet?) clear if this
 * is legal and needs to be accounted for.</p>
 */
public class HeaderValue {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a default {@link HeaderValue} with a zero-length value
     * string and no parameters.</p>
     */
    public HeaderValue() {
        parseHeaderValue("");
    }


    /**
     * <p>Construct a new {@link HeaderValue} instance by parsing the specified
     * header value string via a call to <code>parseHeaderValue()</code>.</p>
     *
     * @param headerValueString A string that includes a value, and optionally
     *  includes name=value parameters delimited by semicolons
     *
     * @exception IllegalArgumentException if the argument is <code>null</code>
     * @exception IllegalArgumentException if the argument contains a comma
     *  (probably indicating that multiple values are present)
     */
    public HeaderValue(String headerValueString) {
        parseHeaderValue(headerValueString);
    }


    // ---------------------------------------------------------- Static Methods


    /**
     * <p>Parse a header values string that may contain one or more header
     * values, separated by commas, and return a list of the discovered
     * {@link HeaderValue} instances.</p>
     *
     * @param headerValuesString Header values string that may contain
     *  one or more header values
     *
     * @exception IllegalArgumentException if headerValuesString is <code>null</code>
     * @exception IllegalArgumentException if one of the header values
     *  cannot be parsed
     */
    public static List<HeaderValue> parseHeaderValues(String headerValuesString) {
        if (headerValuesString == null) {
            throw new IllegalArgumentException("Header values string cannot be null"); // FIXME - I18N
        }
        List<HeaderValue> results = new ArrayList<HeaderValue>();
        if (headerValuesString.indexOf(',') < 0) {
            results.add(new HeaderValue(headerValuesString));
        } else {
            StringTokenizer st = new StringTokenizer(headerValuesString, ",");
            while (st.hasMoreTokens()) {
                results.add(new HeaderValue(st.nextToken()));
            }
        }
        return results;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Map of parameter names and values.</p>
     */
    private Map<String, String> parameters = new HashMap<String,String>();


    /**
     * <p>Value portion of the header value string, omitting any parameters.</p>
     */
    private String value = "";


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return a mutable <code>Map</code> of parameter names and values
     * associated with this header value.  Any names and values added to this
     * <code>Map</code> must be non-<code>null</code> and non-empty.</p>
     */
    public Map<String, String> getParameters() {
        return this.parameters;
    }


    /**
     * <p>Return the value portion of the header value string, without any
     * name=value parameters.</p>
     */
    public String getValue() {
        return this.value;
    }


    /**
     * <p>Parse the specified header value string, <i>replacing</i> the value
     * and any associated parameters.</p>
     *
     * @param headerValueString A string that includes a value, and optionally
     *  includes name=value parameters delimited by semicolons
     *
     * @exception IllegalArgumentException if the argument is <code>null</code>
     * @exception IllegalArgumentException if the argument contains a comma
     *  (probably indicating that multiple values are present)
     */
    public void parseHeaderValue(String headerValueString) {
        if (headerValueString == null) {
            throw new IllegalArgumentException("Value may not be null"); // FIXME - I18N
        } else if (headerValueString.indexOf(',') >= 0) {
            throw new IllegalArgumentException("Value '" + headerValueString + "' may not contain a comma"); // FIXME - I18N
        }
        parameters.clear();
        int semicolon = headerValueString.indexOf(';');
        if (semicolon < 0) {
            setValue(headerValueString);
            return;
        }
        setValue(trim(headerValueString.substring(0, semicolon)));
        StringTokenizer st = new StringTokenizer(headerValueString.substring(semicolon + 1), ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int equals = token.indexOf('=');
            if (equals < 0) {
                throw new IllegalArgumentException("Parameter '" + token.trim() + "' does not include an equals sign"); // FIXME - I18N
            }
            parameters.put(trim(token.substring(0, equals)), trim(token.substring(equals + 1)));
        }
    }


    /**
     * <p>Set only the value portion of the header value string, without
     * affecting any currently stored parameters.</p>
     *
     * @param value The new value portion
     *
     * @exception IllegalArgumentException if the argument is <code>null</code>
     * @exception IllegalArgumentException if the argument includes a semicolon
     *  (';') character
     * @exception IllegalArgumentException if the argument contains a comma
     *  (probably indicating that multiple values are present)
     */
    public void setValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null"); // FIXME - I18N
        } else if (value.indexOf(',') >= 0) {
            throw new IllegalArgumentException("Value '" + value + "' may not include a comma"); // FIXME - I18N
        } else if (value.indexOf(';') >= 0) {
            throw new IllegalArgumentException("Value '" + value + "' may not include a semicolon"); // FIXME - I18N
        }
        this.value = trim(value);
    }


    /**
     * <p>Return the value portion, with any parameters appended, suitable
     * for transmission in an HTTP header in a request or response.</p>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(value);
        for (Map.Entry<String,String> entry : parameters.entrySet()) {
            sb.append(';');
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
        }
        return sb.toString();
    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Trim leading and trailing spaces, as well as optional surrounding
     * double quotes, and return the resulting value.</p>
     *
     * @param value String to be trimmed
     */
    private String trim(String value) {
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim();
    }


}
