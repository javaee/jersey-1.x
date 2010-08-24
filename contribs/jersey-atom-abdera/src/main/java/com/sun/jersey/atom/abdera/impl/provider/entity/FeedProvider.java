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

package com.sun.jersey.atom.abdera.impl.provider.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;

/**
 * <p>JAX-RS Provider for an Atom {@link Feed} Document instance.</p>
 */
//@Provider
@Consumes({"application/atom+xml", "application/xml", "text/xml", "application/atom+json", "application/json"})
@Produces({"application/atom+xml", "application/xml", "text/xml", "application/atom+json", "application/json"})
public class FeedProvider extends AbstractCompletableReaderWriter<Feed> {

    public long getSize(Feed feed, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public boolean isReadable(Class<?> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
        return Feed.class.isAssignableFrom(type);
    }

    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return Feed.class.isAssignableFrom(type);
    }

    public Feed readFrom(Class<Feed> type, Type genericType,
                         Annotation[] annotations, MediaType mediaType,
                         MultivaluedMap<String, String> headers,
                         InputStream stream) throws IOException, WebApplicationException {
        Parser parser = null;
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE) || mediaType.getSubtype().endsWith("+json")) {
            parser = Abdera.getInstance().getParserFactory().getParser("json");
        } else {
            parser = Abdera.getInstance().getParser();
        }
        Document<Feed> document = parser.parse(stream);
        return document.getRoot();
    }

    public void writeTo(Feed feed, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> headers,
                        OutputStream stream) throws IOException, WebApplicationException {
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE) || mediaType.getSubtype().endsWith("+json")) {
            feed.writeTo("json", stream);
        } else {
            feed.writeTo(stream);
        }
    }

}
