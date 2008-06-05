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

package com.sun.jersey.impl.container.servlet;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.spi.container.AbstractContainerResponse;
import com.sun.jersey.spi.container.WebApplication;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Adapts a HttpServletResponse to provide the methods of HttpResponse
 *
 */
public final class HttpResponseAdaptor extends AbstractContainerResponse {
    private final ServletContext context;
            
    private final HttpServletRequest request;
    
    private final HttpServletResponse response;
    
    private RequestDispatcher d;
        
    private OutputStream out;
    
    public HttpResponseAdaptor(ServletContext context, 
            HttpServletResponse response, 
            HttpServletRequest request, 
            WebApplication wa,
            HttpRequestAdaptor requestContext) {
        super(wa, requestContext);
        this.context = context;
        this.response = response;
        this.request = request;
    }

    
    // HttpResponseContextImpl
    
    final class OutputStreamAdapter extends OutputStream {
        OutputStream out;
        
        public void write(int b) throws IOException {
            initiate();
            out.write(b);
        }

        public void write(byte b[]) throws IOException {
            initiate();
            out.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            initiate();
            out.write(b, off, len);
        }

        public void flush() throws IOException {
            initiate();
            out.flush();
        }

        public void close() throws IOException {
            initiate();
            out.close();
        }
        
        void initiate() throws IOException {
            if (out == null)
                out = response.getOutputStream();
        }
    }
    
    protected OutputStream getUnderlyingOutputStream() throws IOException {
        if (out == null)
            out = new OutputStreamAdapter();
        
        return out;
    }

    protected void commitStatusAndHeaders(long contentLength) throws IOException {
        response.setStatus(this.getStatus());
        if (contentLength != -1 && contentLength < Integer.MAX_VALUE) 
            response.setContentLength((int)contentLength);
        
        MultivaluedMap<String, Object> headers = this.getHttpHeaders();
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            for (Object v : e.getValue()) {
                response.addHeader(e.getKey(), getHeaderValue(v));
            }
        }
    }

    
    public void commitAll() throws IOException {
        if (isCommitted()) return;
        
        if (response.isCommitted()) return;
    
        writeEntity();
    }    
    
    public  RequestDispatcher getRequestDispatcher() {
        return d;
    }
        
    /* package */ void forwardTo(String path, Object it) {        
        d = context.getRequestDispatcher(path);
        if (d == null) {
            throw new ContainerException("No request dispatcher for: " + path);
        }
        
        d = new RequestDispatcherWrapper(d, it);
        // TODO may need to forward immediately
    }    
}