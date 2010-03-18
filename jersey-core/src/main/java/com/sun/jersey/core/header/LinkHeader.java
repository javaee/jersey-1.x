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

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import javax.ws.rs.core.MultivaluedMap;

/**
 * LinkHeader class.
 *
 * TODO: Verify if this simple parser obeys formal grammar
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class LinkHeader {

    private URI uri;

    private final MultivaluedMap<String, String> params;

    public LinkHeader(String header) throws IllegalArgumentException {
        if (header == null)
            throw new IllegalArgumentException("header parameter MUST NOT be null");

        params = parseHeader(new StringTokenizer(header, " <>;=\"", true));
    }

    protected LinkHeader(URI uri, MultivaluedMap<String, String> params) {
        this.uri = uri;
        this.params = params;
    }

    public static LinkHeader valueOf(String header) {
        return new LinkHeader(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(uri.toASCIIString()).append(">");
        for (Entry<String, List<String>> e : params.entrySet()) {
            for (String value : e.getValue()) {
                sb.append(";").append(e.getKey()).append("=").append(value);
            }
        }
        return sb.toString();
    }

    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    public URI getUri() {
        return uri;
    }

    public String getRel() {
        return params.getFirst("rel");
    }

    public String getOp() {
        return params.getFirst("op");
    }
    
    private MultivaluedMap<String, String> parseHeader(StringTokenizer st) {
        String token = st.nextToken();
        if (token.charAt(0) != '<') {
            throw new RuntimeException("Unexpected token '" +
                    token + "' in link header");
        }
        uri = URI.create(st.nextToken());
        token = st.nextToken();
        if (token.charAt(0) != '>') {
            throw new RuntimeException("Unexpected token '" +
                    token + "' in link header");
        }
        return parseParams(st);
    }

    private static MultivaluedMap<String, String> parseParams(StringTokenizer st) {
        String name = null;
        boolean inQuotes = false;
        boolean parsingValue = false;
        StringBuilder value = new StringBuilder();

        MultivaluedMap params = new MultivaluedMapImpl();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            switch (token.charAt(0)) {
                case '=':
                    parsingValue = true;
                    break;
                case ';':
                    if (parsingValue) {
                        params.add(name, value.toString());
                        value.setLength(0);
                        parsingValue = false;
                    }
                    break;
                case '"':
                    assert parsingValue;
                    if (inQuotes) {
                        params.add(name, value.toString());
                        value.setLength(0);
                    } else {
                        inQuotes = true;
                    }
                case ' ':
                    if (inQuotes) {
                        value.append(' ');
                    } else if (parsingValue) {
                        params.add(name, value.toString());
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
        params.add(name, value.toString());

        return params;
    }

    public static LinkHeaderBuilder uri(URI uri) {
        return new LinkHeaderBuilder(uri);
    }

    public static class LinkHeaderBuilder<T extends LinkHeaderBuilder, V extends LinkHeader> {
        protected URI uri;

        protected MultivaluedMap<String, String> params;

        LinkHeaderBuilder(URI uri) {
            this.uri = uri;
        }

        public T rel(String rel) {
            addParam("rel", rel);
            return (T)this;
        }

        public T op(String op) {
            addParam("op", op);
            return (T)this;
        }

        private void addParam(String key, String value) {
            if (params == null)
                params = new MultivaluedMapImpl();
            params.add(key, value);
        }

        public V build() {
            LinkHeader lh = new LinkHeader(uri, new MultivaluedMapImpl(params));
            return (V)lh;
        }
    }
}
