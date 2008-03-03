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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.ws.rest.spi.container.AbstractContainerResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.ws.rest.spi.container.MessageBodyContext;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public final class GrizzlyResponseAdaptor extends AbstractContainerResponse {
    
    private final GrizzlyResponse response;
    
    /* package */ GrizzlyResponseAdaptor(GrizzlyResponse response, 
            MessageBodyContext bodyContext, GrizzlyRequestAdaptor requestContext) {
        super(bodyContext, requestContext);
        this.response = response;
    }
    
    
    protected void commitStatusAndHeaders() throws IOException {
        response.setStatus(this.getStatus());
        
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            String key = e.getKey();
            for (Object value: e.getValue()) {
                response.addHeader(key,value.toString());
            }
        }

        String contentType = response.getHeader("Content-Type");
        if (contentType != null) {
            response.setContentType(contentType);
        }
    }    
    
    /* package */ void commitAll() throws IOException {
        if (isCommitted()) {
            getUnderlyingOutputStream().close();
            return;
        }
        
        commitStatusAndHeaders();
        
        final OutputStream out = response.getOutputStream();
        writeEntity(out);
        out.close();
    }

    protected OutputStream getUnderlyingOutputStream() throws IOException {
        return response.getOutputStream();
    }
}
