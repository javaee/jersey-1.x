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
package com.sun.jersey.api.client.filter;

import com.sun.jersey.api.client.AbstractClientRequestAdapter;
import com.sun.jersey.api.client.ClientFilter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.HttpHeaders;

/**
 * A GZIP content encoding filter.
 * <p>
 * The request will be modified to set the Accept-Encoding header to "gzip"
 * if that header has not already been set by the client.
 * <p>
 * If the request contains an entity and a Content-Encoding header of "gzip"
 * then the entity will be compressed using gzip.
 * If configured, and there does not exist a Content-Encoding header of "gzip"
 * then such a header is added to the request and the entity will be compressed
 * using gzip.
 * <p>
 * If the response has a Content-Encoding header of "gzip" then
 * then the response entity will be uncompressed using gzip.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class GZIPContentEncodingFilter extends ClientFilter {

    private static final class Adapter extends AbstractClientRequestAdapter {
        Adapter(ClientRequestAdapter cra) {
            super(cra);
        }

        public OutputStream adapt(ClientRequest request, OutputStream out) throws IOException {
            return new GZIPOutputStream(getAdapter().adapt(request, out));
        }
        
    }

    private final boolean compressRequestEntity;

    /**
     * Create a GZIP Content-Encoding filter that compresses the request
     * entity.
     */
    public GZIPContentEncodingFilter() {
        this(true);
    }
    
    /**
     * Create a GZIP Content-Encoding filter.
     * 
     * @param compressRequestEntity if true the request entity (if any)
     *        is always compressed, otherwise the request entity is compressed
     *        only if there exists a Content-Encoding header whose
     *        value is "gzip".
     */
    public GZIPContentEncodingFilter(boolean compressRequestEntity) {
        this.compressRequestEntity = compressRequestEntity;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        if (!request.getMetadata().containsKey(HttpHeaders.ACCEPT_ENCODING)) {
            request.getMetadata().add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        }

        if (request.getEntity() != null) {
            Object o = request.getMetadata().getFirst(HttpHeaders.CONTENT_ENCODING);
            if (o != null && o.equals("gzip")) {
                request.setAdapter(new Adapter(request.getAdapter()));
            } else if (compressRequestEntity) {
                request.getMetadata().add(HttpHeaders.CONTENT_ENCODING, "gzip");
                request.setAdapter(new Adapter(request.getAdapter()));
            }
        }        
        
        ClientResponse response = getNext().handle(request);
        
        if (response.hasEntity() &&
                response.getMetadata().containsKey(HttpHeaders.CONTENT_ENCODING)) {
            String encodings = response.getMetadata().getFirst(HttpHeaders.CONTENT_ENCODING);

            if (encodings.equals("gzip")) {
                response.getMetadata().remove(HttpHeaders.CONTENT_ENCODING);
                try {
                    response.setEntityInputStream(new GZIPInputStream(response.getEntityInputStream()));
                } catch (IOException ex) {
                    throw new ClientHandlerException(ex);
                }
            }
        }
        
        return response;
    }
}