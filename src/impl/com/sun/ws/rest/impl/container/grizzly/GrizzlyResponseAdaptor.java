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
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.http.MimeHeaders;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public final class GrizzlyResponseAdaptor extends AbstractContainerResponse {
    
    private final Response response;
    
    private OutputStream output;

    
    /* package */ GrizzlyResponseAdaptor(Response response, GrizzlyRequestAdaptor requestContext) {
        super(requestContext);
        this.response = response;
    }
    
    
    // HttpResponseContextImpl

    protected OutputStream getUnderlyingOutputStream() throws IOException {
        if (output != null)
            return output;
        
        return output = new GrizzlyResponseOutputStream();
    }
    
    protected void commitStatusAndHeaders() throws IOException {
        response.setStatus(this.getStatus());
        
        MimeHeaders mh = response.getMimeHeaders();
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            String key = e.getKey();
            for (Object value: e.getValue()) {
                MessageBytes mb = mh.addValue(key);
                mb.setString(getHeaderValue(value));
            }
        }

        if (mh.getValue("Content-Type") != null) {
            response.setContentType(mh.getValue("Content-Type").getString());
        }
        
        response.sendHeaders();
    }    
    
    /* package */ void commitAll() throws IOException {
        if (isCommitted())
            return;
        
        commitStatusAndHeaders();
        
        final OutputStream out = getUnderlyingOutputStream();
        writeEntity(out);
        out.close();
    }
        
    private final class GrizzlyResponseOutputStream extends OutputStream {
        public final static int BUFFER_SIZE = 4096;
        
        final ByteChunk chunk;
        
        public GrizzlyResponseOutputStream() {
            chunk = new ByteChunk(BUFFER_SIZE);
        }
        
        public void write(int b) throws IOException {
            if (chunk.getLength() > BUFFER_SIZE)
                flush();
            chunk.append((byte)b);
        }
        
        public void write(byte[] b) throws IOException {
            if (chunk.getLength() > BUFFER_SIZE)
                flush();
            chunk.append(b, 0, b.length);
        }
        
        public void write(byte[] b, int off, int len) throws IOException {
            if (chunk.getLength() > BUFFER_SIZE)
                flush();
            chunk.append(b, off, len);
        }
        
        public void flush() throws IOException {
            response.doWrite(chunk);
            chunk.reset();
        }
        
        public void close() throws IOException {
            flush();
            chunk.recycle();
        }
    }
}
