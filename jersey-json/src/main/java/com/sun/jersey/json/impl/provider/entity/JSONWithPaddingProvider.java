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


package com.sun.jersey.json.impl.provider.entity;

import com.sun.jersey.api.json.JSONWithPadding;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.jersey.json.impl.ImplMessages;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author Jakub.Podlesak@Sun.COM, Paul.Sandoz@Sun.COM
 */
public class JSONWithPaddingProvider extends AbstractMessageReaderWriterProvider<JSONWithPadding> {

    private static final Logger LOGGER = Logger.getLogger(JSONWithPaddingProvider.class.getName());

    private final Map<String, Set<String>> javascriptTypes;

    @Context MessageBodyWorkers bodyWorker;


    public JSONWithPaddingProvider() {
        javascriptTypes = new HashMap<String, Set<String>>();
        // application/javascript, application/x-javascript, text/ecmascript, application/ecmascript, text/jscript
        javascriptTypes.put("application", new HashSet<String>(Arrays.asList("x-javascript", "ecmascript", "javascript")));
        javascriptTypes.put("text", new HashSet<String>(Arrays.asList("ecmascript", "jscript")));
    }

    private boolean isJavascript(MediaType m) {
        Set<String> subtypes = javascriptTypes.get(m.getType());
        if (subtypes == null) return false;

        return subtypes.contains(m.getSubtype());
    }
    

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    public JSONWithPadding readFrom(Class<JSONWithPadding> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        throw new UnsupportedOperationException("Not supported by design.");
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == JSONWithPadding.class;
    }

    public void writeTo(JSONWithPadding t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        Object jsonEntity = t.getJsonSource();
        Type entityGenericType = jsonEntity.getClass();
        Class<?> entityType = jsonEntity.getClass();

        final boolean genericEntityUsed = jsonEntity instanceof GenericEntity;

        if (genericEntityUsed) {
            GenericEntity ge = (GenericEntity)jsonEntity;
            jsonEntity = ge.getEntity();
            entityGenericType = ge.getType();
            entityType = ge.getRawType();
        }

        final boolean isJavaScript = isJavascript(mediaType);
        final MediaType workerMediaType = isJavaScript ? MediaType.APPLICATION_JSON_TYPE : mediaType;

        MessageBodyWriter bw = bodyWorker.getMessageBodyWriter(entityType, entityGenericType, annotations, workerMediaType);
        if (bw == null) {
            if (!genericEntityUsed) {
                LOGGER.severe(ImplMessages.ERROR_NONGE_JSONP_MSG_BODY_WRITER_NOT_FOUND(jsonEntity, workerMediaType));
            } else {
                LOGGER.severe(ImplMessages.ERROR_JSONP_MSG_BODY_WRITER_NOT_FOUND(jsonEntity, workerMediaType));
            }
            throw new WebApplicationException();
        }


        if (isJavaScript) {
            entityStream.write(t.getCallbackName().getBytes());
            entityStream.write('(');
        }

        bw.writeTo(jsonEntity, entityType, entityGenericType, annotations, workerMediaType, httpHeaders, entityStream);

        if (isJavaScript) {
            entityStream.write(')');
        }
    }
}
