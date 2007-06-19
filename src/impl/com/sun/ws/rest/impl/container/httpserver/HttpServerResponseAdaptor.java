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
import com.sun.ws.rest.impl.HttpResponseContextImpl;
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
public class HttpServerResponseAdaptor extends HttpResponseContextImpl {
    
    private final HttpExchange exchange;
    
    private OutputStream out;
    
    public HttpServerResponseAdaptor(HttpExchange exchange, HttpServerRequestAdaptor requestContext) {
        super(requestContext);
        this.exchange = exchange;
    }

    @SuppressWarnings("unchecked")
    public void commit() throws IOException {
        Headers eh = exchange.getResponseHeaders();
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object v : e.getValue())
                values.add(getHeaderValue(v));
            eh.put(e.getKey(), values);
        }
        
        Object entity = this.getResponse().getEntity();
        if (entity != null) {
            exchange.sendResponseHeaders(this.getResponse().getStatus(), 0);
            
            final EntityProvider p = ProviderFactory.newInstance().createEntityProvider(entity.getClass());
            p.writeTo(entity, this.getHttpHeaders(), this.getOutputStream());
            if (out != null) {
                out.flush();
                out.close();
            }
        } else {
            exchange.sendResponseHeaders(this.getResponse().getStatus(), -1);
        }
        exchange.close();        
    }

    // HttpResponseContext
    
    public OutputStream getOutputStream() throws IOException {
        if (out != null)
            return out;
        
        return out = exchange.getResponseBody();
    }
}
