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

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.ws.rest.spi.container.AbstractContainerResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 * A HTTP response adapter for {@link HttpExchange}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpServerResponseAdaptor extends AbstractContainerResponse {
    
    private final HttpExchange exchange;
    
    /* package */ HttpServerResponseAdaptor(HttpExchange exchange, HttpServerRequestAdaptor requestContext) {
        super(requestContext);
        this.exchange = exchange;
    }

    // HttpResponseContextImpl
        
    protected OutputStream getUnderlyingOutputStream() throws IOException {
        return exchange.getResponseBody();
    }

    protected void commitStatusAndHeaders() throws IOException {
        commitHeaders();
        
        exchange.sendResponseHeaders(this.getStatus(), 0);
    }
    
    //
    
    private void commitHeaders() throws IOException {
        Headers eh = exchange.getResponseHeaders();
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object v : e.getValue())
                values.add(getHeaderValue(v));
            eh.put(e.getKey(), values);
        }
    }
    
    /* package */ void commitAll() throws IOException {
        if (isCommitted()) {
            exchange.close();        
            return;
        }
        
        commitHeaders();
        
        Object entity = this.getEntity();
        if (entity != null) {
            exchange.sendResponseHeaders(this.getStatus(), 0);
            writeEntity(entity, getUnderlyingOutputStream());
        } else {
            exchange.sendResponseHeaders(this.getStatus(), -1);
        }
        // This is required for the LW HTTP server shipped with Java SE 6
        // exchange.close() does not work as documented
        exchange.getResponseBody().close();
        exchange.close();        
    }

}
