/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.core.header;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * LinkHeader class.
 *
 * TODO: Verify if this simple parser obeys formal grammar
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class LinkHeader {

    private String uri;

    private Map<String, String> params = new HashMap<String, String>();

    public LinkHeader() {
    }

    public LinkHeader(String header) {
        assert header != null;
        StringTokenizer st = new StringTokenizer(header, " <>;=\"", true);
        parseHeader(st);
    }

    public static LinkHeader valueOf(String header) {
        return new LinkHeader(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(uri).append(">");
        for (Entry<String, String> e : params.entrySet()) {
            sb.append(";").append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getUri() {
        return uri;
    }

    public String getRel() {
        return params.get("rel");
    }

    public String getOp() {
        return params.get("op");
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setRel(String s) {
        params.put("rel", s);
    }

    public void setOp(String s) {
        params.put("op", s);
    }

    private void parseHeader(StringTokenizer st) {
        String token = st.nextToken();
        if (token.charAt(0) != '<') {
            throw new RuntimeException("Unexpected token '" +
                    token + "' in link header");
        }
        uri = st.nextToken();
        token = st.nextToken();
        if (token.charAt(0) != '>') {
            throw new RuntimeException("Unexpected token '" +
                    token + "' in link header");
        }
        parseParams(st);
    }

    private void parseParams(StringTokenizer st) {
        String name = null;
        boolean inQuotes = false;
        boolean parsingValue = false;
        StringBuilder value = new StringBuilder();

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            switch (token.charAt(0)) {
                case '=':
                    parsingValue = true;
                    break;
                case ';':
                    if (parsingValue) {
                        params.put(name, value.toString());
                        value.setLength(0);
                        parsingValue = false;
                    }
                    break;
                case '"':
                    assert parsingValue;
                    if (inQuotes) {
                        params.put(name, value.toString());
                        value.setLength(0);
                    } else {
                        inQuotes = true;
                    }
                case ' ':
                    if (inQuotes) {
                        value.append(' ');
                    } else if (parsingValue) {
                        params.put(name, value.toString());
                        value.setLength(0);
                    }
                    break;
                default:
                    if (!parsingValue) {
                        name = token;
                    } else {
                        value.append(token);
                    }
                    break;
            }
        }

        // Last parameter parsed
        params.put(name, value.toString());
    }

}
