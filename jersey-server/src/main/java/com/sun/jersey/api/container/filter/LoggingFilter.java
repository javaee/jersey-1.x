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
package com.sun.jersey.api.container.filter;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A logging filter.
 * <p>
 * The request headers, request entity, response headers and response entity
 * will be logged. By default logging will be output to System.out.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey filter can be
 * registered using the following initialization parameters:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.LoggingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 *     &lt;init-param&gt
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerResponseFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.LoggingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * </pre></blockquote>
 *
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.container.filter
 */
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String NOTIFICATION_PREFIX = "* ";
    
    private static final String REQUEST_PREFIX = "> ";
    
    private static final String RESPONSE_PREFIX = "< ";

    private @Context HttpContext hc;
    
    private long id = 0;

    private final PrintStream loggingStream;
    
    public LoggingFilter() {
        this(System.out);
    }
    
    public LoggingFilter(PrintStream loggingStream) {
        this.loggingStream = loggingStream;
    }

    private synchronized void setId() {
        hc.getProperties().put("request-id", Long.toString(++id));
    }

    private PrintStream prefixId() {
        loggingStream.append(hc.getProperties().get("request-id").toString()).
                append(" ");
        return loggingStream;
    }
    
    public ContainerRequest filter(ContainerRequest request) {
        setId();
        printRequestLine(request);
        printRequestHeaders(request.getRequestHeaders());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = request.getEntityInputStream();
        try {
            int read;
            final byte[] data = new byte[2048];
            while ((read = in.read(data)) != -1)
                out.write(data, 0, read);

            byte[] requestEntity = out.toByteArray();
            printRequestEntity(requestEntity);
            request.setEntityInputStream(new ByteArrayInputStream(requestEntity));
            return request;
        } catch (IOException ex) {
            throw new ContainerException(ex);
        }

    }
    
    private void printRequestLine(ContainerRequest request) {
        prefixId().append(NOTIFICATION_PREFIX).append("In-bound request received").println();
        prefixId().append(REQUEST_PREFIX).append(request.getMethod()).append(" ").
                append(request.getRequestUri().toASCIIString()).println();
    }
    
    private void printRequestHeaders(MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId().append(REQUEST_PREFIX).append(header).append(": ").
                        append(value).println();                
            }
        }
        prefixId().println("> ");
    }

    private void printRequestEntity(byte[] requestEntity) throws IOException {
        if (requestEntity.length == 0)
            return;
        loggingStream.write(requestEntity);
        loggingStream.println();        
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
                prefixId().append(NOTIFICATION_PREFIX).
                        append("Out-bound response sent").println();
                init = false;
            }            
        }
    }

    private final class Adapter implements ContainerResponseWriter {
        private final ContainerResponseWriter crw;
        private LoggingOutputStream out;

        Adapter(ContainerResponseWriter crw) {
            this.crw = crw;
        }
        
        public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
           printResponseLine(response);
           printResponseHeaders(response.getHttpHeaders());           
           return out = new LoggingOutputStream(crw.writeStatusAndHeaders(-1, response));
        }

        public void finish() throws IOException {
            out.finish();
        }
    }

    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        response.setContainerResponseWriter(
                new Adapter(response.getContainerResponseWriter()));
        return response;
    }
    
    private void printResponseLine(ContainerResponse response) {
        prefixId().append(RESPONSE_PREFIX).append(Integer.toString(response.getStatus())).println();
    }
    
    private void printResponseHeaders(MultivaluedMap<String, Object> headers) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            String header = e.getKey();
            for (Object value : e.getValue()) {
                prefixId().append(RESPONSE_PREFIX).append(header).append(": ").
                        append(ContainerResponse.getHeaderValue(value)).println();                
            }
        }
        prefixId().println(RESPONSE_PREFIX);
    } 
}