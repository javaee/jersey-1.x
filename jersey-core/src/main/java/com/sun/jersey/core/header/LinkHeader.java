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
package com.sun.jersey.core.header;

import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.jersey.core.header.reader.HttpHeaderReader.Event;
import com.sun.jersey.core.impl.provider.header.WriterUtil;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A Link header.
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @author Paul.Sandoz@sun.com
 */
public class LinkHeader {

    private URI uri;

    private Set<String> rels;

    private MediaType type;
    
    private MultivaluedMap<String, String> parameters;

    public LinkHeader(String header) throws ParseException, IllegalArgumentException {
        this(HttpHeaderReader.newInstance(header));
    }

    public LinkHeader(HttpHeaderReader reader) throws ParseException, IllegalArgumentException {
        uri = URI.create(reader.nextSeparatedString('<', '>'));

        if (reader.hasNext())
            parseParameters(reader);
    }

    protected LinkHeader(LinkHeaderBuilder builder) {
        this.uri = builder.uri;

        if (builder.rels != null) {
            if (builder.rels.size() == 1) {
                this.rels = builder.rels;
            } else {
                this.rels = Collections.unmodifiableSet(new HashSet<String>(builder.rels));
            }
        }

        this.type = builder.type;
        
        if (builder.parameters != null) {
            this.parameters = new MultivaluedMapImpl(builder.parameters);
        }
    }

    public static LinkHeader valueOf(String header) throws IllegalArgumentException {
        try {
            return new LinkHeader(HttpHeaderReader.newInstance(header));
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append('<').append(uri.toASCIIString()).append('>');

        if (rels != null) {
            sb.append(';').append("rel=");
            if (rels.size() == 1) {
                sb.append(rels.iterator().next());
            } else {
                sb.append('\"');
                boolean first = true;
                for (String rel : rels) {
                    if (!first)
                        sb.append(' ');
                    sb.append(rel);
                    first = false;
                }
                sb.append('\"');
            }
        }

        if (type != null) {
            sb.append(';').append("type=").
                    append(type.getType()).append('/').append(type.getSubtype());
        }

        if (parameters != null) {
            for (Entry<String, List<String>> e : parameters.entrySet()) {
                String key = e.getKey();
                List<String> values = e.getValue();

                if (key.equals("anchor") || key.equals("title")) {
                    sb.append(";").append(key).append("=");
                    WriterUtil.appendQuoted(sb, values.get(0));
                } else if (key.equals("hreflang")) {
                    for (String value : e.getValue()) {
                        sb.append(";").append(e.getKey()).append("=").
                                append(value);
                    }
                } else {
                    for (String value : e.getValue()) {
                        sb.append(";").append(e.getKey()).append("=");                    
                        WriterUtil.appendQuoted(sb, value);
                    }                    
                }
            }
        }
        
        return sb.toString();
    }

    public MultivaluedMap<String, String> getParams() {
        checkNull();
        return parameters;
    }

    public URI getUri() {
        return uri;
    }

    public Set<String> getRel() {
        if (rels == null) {
            rels = Collections.emptySet();
        }
        return rels;
    }

    public MediaType getType() {
        return type;
    }
    
    public String getOp() {
        if (parameters != null) {
            return parameters.getFirst("op");
        } else {
            return null;
        }
    }

    private void parseParameters(HttpHeaderReader reader) throws ParseException {
        while (reader.hasNext()) {
            reader.nextSeparator(';');
            while(reader.hasNextSeparator(';', true))
                reader.next();

            // Ignore a ';' with no parameters
            if (!reader.hasNext())
                break;

            // Get the parameter name
            String name = reader.nextToken().toLowerCase();
            reader.nextSeparator('=');

            if (name.equals("rel")) {
                String value = reader.nextTokenOrQuotedString();
                if (reader.getEvent() == Event.Token) {
                    rels = Collections.singleton(value);
                } else {
                    String[] values = value.split(" ");
                    rels = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
                }
            } else if (name.equals("hreflang")) {
                add(name, reader.nextTokenOrQuotedString());
            } else if (name.equals("media")) {
                if (!containsKey("media")) {
                    add(name, reader.nextTokenOrQuotedString());
                }
            } else if (name.equals("title")) {
                if (!containsKey("title")) {
                    add(name, reader.nextQuotedString());
                }
            } else if (name.equals("title*")) {
                add(name, reader.nextQuotedString());
            } else if (name.equals("type")) {
                String typeName = reader.nextToken();
                reader.nextSeparator('/');
                String subTypeName = reader.nextToken();
                type = new MediaType(typeName, subTypeName);
            } else {
                add(name, reader.nextTokenOrQuotedString());
            }

            // Get the parameter value
        }
    }

    private void checkNull() {
        if (parameters == null)
            parameters = new MultivaluedMapImpl();
    }

    private boolean containsKey(String key) {
        checkNull();
        return parameters.containsKey(key);
    }

    private void add(String key, String value) {
        checkNull();
        parameters.add(key, value);
    }

    public static LinkHeaderBuilder uri(URI uri) {
        return new LinkHeaderBuilder(uri);
    }

    /**
     * A Link header builder.
     * 
     */
    public static class LinkHeaderBuilder<T extends LinkHeaderBuilder, V extends LinkHeader> {
        protected URI uri;

        protected Set<String> rels;

        protected MediaType type;
        
        protected MultivaluedMap<String, String> parameters;

        LinkHeaderBuilder(URI uri) {
            this.uri = uri;
        }

        public T rel(String rel) {
            if (rel == null)
                throw new IllegalArgumentException("rel parameter cannot be null");

            rel = rel.trim();
            if (rel.length() == 0)
                throw new IllegalArgumentException("rel parameter cannot an empty string or just white space");

            if (rels == null) {
                rels = Collections.singleton(rel);
            } else if (rels.size() == 1 && !rels.contains(rel)) {
                rels = new HashSet<String>(rels);
                rels.add(rel);
            } else {
                rels.add(rel);
            }
            
            return (T)this;
        }

        public T type(MediaType type) {
            this.type = type;
            return (T)this;
        }
        
        public T op(String op) {
            parameter("op", op);
            return (T)this;
        }

        public T parameter(String key, String value) {
            if (key.equals("rel")) {
                return rel(value);
            } else if (key.equals("type")) {
                return type(MediaType.valueOf(value));
            }
            
            if (parameters == null)
                parameters = new MultivaluedMapImpl();
            parameters.add(key, value);
            return (T)this;
        }

        public V build() {
            LinkHeader lh = new LinkHeader(this);
            return (V)lh;
        }
    }
}
