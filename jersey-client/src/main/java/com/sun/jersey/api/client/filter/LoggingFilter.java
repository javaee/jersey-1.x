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
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A logging filter.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class LoggingFilter extends ClientFilter {
    private static final String NOTIFICATION_PREFIX = "* ";
    
    private static final String REQUEST_PREFIX = "> ";
    
    private static final String RESPONSE_PREFIX = "< ";
    
    private final class Adapter extends AbstractClientRequestAdapter {
        Adapter(ClientRequestAdapter cra) {
            super(cra);
        }

        public OutputStream adapt(ClientRequest request, OutputStream out) throws IOException {
            return new LoggingOutputStream(getAdapter().adapt(request, out));
        }
        
    }

    private final class LoggingOutputStream extends OutputStream {
        private boolean init = false;
        private OutputStream out;

        LoggingOutputStream(OutputStream out) {
            this.out = out;
        }
        
        @Override
        public void write(byte[] b)  throws IOException {
            init();
            loggingStream.write(b);
            out.write(b);
        }
    
        @Override
        public void write(byte[] b, int off, int len)  throws IOException {
            init();
            loggingStream.write(b, off, len);
            out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            init();
            loggingStream.write(b);
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            finish();
            out.close();
        }
        
        private final void init() {
            if (init == false) {
                init = true;
            }
        }
        
        private final void finish() {
            if (init) {
                loggingStream.println();
                init = false;
            }            
        }
    }

    private final PrintStream loggingStream;

    private long _id = 0;
    
    public LoggingFilter() {
        this(System.out);
    }
    
    public LoggingFilter(PrintStream loggingStream) {
        this.loggingStream = loggingStream;
    }

    private PrintStream prefixId(long id) {
        loggingStream.append(Long.toString(id)).append(" ");
        return loggingStream;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        long id = ++this._id;

        printRequestLine(id, request);
        printRequestHeaders(id, request.getMetadata());
            
        request.setAdapter(new Adapter(request.getAdapter()));
        
        ClientResponse response = getNext().handle(request);

        printResponseLine(id, response);
        printResponseHeaders(id, response.getMetadata());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = response.getEntityInputStream();
        try {
            int read;
            final byte[] data = new byte[2048];
            while ((read = in.read(data)) != -1)
                out.write(data, 0, read);

            byte[] requestEntity = out.toByteArray();
            printResponseEntity(requestEntity);
            response.setEntityInputStream(new ByteArrayInputStream(requestEntity));
        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
        prefixId(id).append(NOTIFICATION_PREFIX).
                append("In-bound response").println();

        return response;
    }
    
    private void printRequestLine(long id, ClientRequest request) {
        prefixId(id).append(NOTIFICATION_PREFIX).append("Out-bound request").println();
        prefixId(id).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").
                append(request.getURI().toASCIIString()).println();
    }
    
    private void printRequestHeaders(long id, MultivaluedMap<String, Object> headers) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            String header = e.getKey();
            for (Object value : e.getValue()) {
                prefixId(id).append(REQUEST_PREFIX).append(header).append(": ").
                        append(ClientRequest.getHeaderValue(value)).println();                
            }
        }
        prefixId(id).println("> ");
    }
    
    private void printResponseLine(long id, ClientResponse response) {
        prefixId(id).append(RESPONSE_PREFIX).append(Integer.toString(response.getStatus())).println();
    }
    
    private void printResponseHeaders(long id, MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId(id).append(RESPONSE_PREFIX).append(header).append(": ").
                        append(value).println();                
            }
        }
        prefixId(id).println(RESPONSE_PREFIX);
    }

    private void printResponseEntity(byte[] responseEntity) throws IOException {
        if (responseEntity.length == 0)
            return;
        loggingStream.write(responseEntity);
        loggingStream.println();        
    }   
}