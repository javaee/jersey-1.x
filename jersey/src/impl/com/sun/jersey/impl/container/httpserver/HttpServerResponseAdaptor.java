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

package com.sun.jersey.impl.container.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.jersey.spi.container.AbstractContainerResponse;
import com.sun.jersey.spi.container.MessageBodyContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A HTTP response adapter for {@link HttpExchange}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpServerResponseAdaptor extends AbstractContainerResponse {
    
    private final HttpExchange exchange;
    
    /* package */ HttpServerResponseAdaptor(HttpExchange exchange, MessageBodyContext bodyContext,
            HttpServerRequestAdaptor requestContext) {
        super(bodyContext, requestContext);
        this.exchange = exchange;
    }

    // HttpResponseContextImpl
        
    protected OutputStream getUnderlyingOutputStream() throws IOException {
        return exchange.getResponseBody();
    }

    protected void commitStatusAndHeaders(long contentLength) throws IOException {
        commitHeaders();
        exchange.sendResponseHeaders(this.getStatus(), 
                contentLength == -1 ? 0 : contentLength);
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
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
            exchange.close();        
            return;
        }
        
        if (this.getEntity() != null) {
            writeEntity();
        } else {
            commitHeaders();
            exchange.sendResponseHeaders(this.getStatus(), -1);
        }
        
        // This is required for the LW HTTP server shipped with Java SE 6
        // exchange.close() does not work as documented
        exchange.getResponseBody().flush();
        exchange.getResponseBody().close();
        exchange.close();        
    }
}
