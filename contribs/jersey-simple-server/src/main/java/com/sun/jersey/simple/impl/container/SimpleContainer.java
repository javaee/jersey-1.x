/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.simple.impl.container;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.simpleframework.http.Address;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This is the container that handles all HTTP requests. Requests are adapted
 * for the enclosed {@link WebApplication} instances. This container can
 * service both HTTP and HTTPS traffic transparently, when created using the
 * factory methods of {@link com.sun.jersey.api.simple.container.SimpleServerFactory} 
 * or when instantiating it and making a direct connection to the container.
 * 
 * @author Marc.Hadley@Sun.Com
 */
public final class SimpleContainer implements Container, ContainerListener {
    
    private WebApplication application;
    
    public SimpleContainer(WebApplication application) throws ContainerException {
        this.application = application;
    }

    private final static class Writer implements ContainerResponseWriter {
        final Response response;
        final Request request;
        
        Writer(Request request, Response response) {
            this.response = response;
            this.request = request;
        }
        
        public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse cResponse) throws IOException {           
            int code = cResponse.getStatus();
            String text = Status.getDescription(code);
            String method = request.getMethod();

            response.setCode(code);
            response.setText(text);

            if (!method.equalsIgnoreCase("HEAD") && contentLength != -1 && contentLength < Integer.MAX_VALUE) {
                response.setContentLength((int)contentLength);
            }
            for (Map.Entry<String, List<Object>> e : cResponse.getHttpHeaders().entrySet()) {
                for (Object value : e.getValue()) {
                    response.add(e.getKey(), ContainerResponse.getHeaderValue(value));
                }
            }
            return response.getOutputStream();
        }
        
        public void finish() throws IOException {           
           response.close(); 
        }
    }
    
    public void handle(Request request, Response response) {
        WebApplication target = application;
        
        final URI baseUri = getBaseUri(request);
        final URI requestUri = baseUri.resolve(request.getTarget());
        
        try {
            final ContainerRequest cRequest = new ContainerRequest(
                    target, 
                    request.getMethod(), 
                    baseUri, 
                    requestUri, 
                    getHeaders(request), 
                    request.getInputStream());
           
                target.handleRequest(cRequest, new Writer(request, response));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
           close(response);
        }
    }
    
    private void close(Response response) {
       try {
          response.close();
       } catch(Exception ex) {
          throw new RuntimeException(ex);
       }
    }

    private URI getBaseUri(Request request) {
        try {
            final Address address = request.getAddress();

            return new URI(
                    address.getScheme(), 
                    null, 
                    address.getDomain(),
                    address.getPort(), 
                    "/", 
                    null, null);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private InBoundHeaders getHeaders(Request request) {
        InBoundHeaders header = new InBoundHeaders();
        
        List<String> names = request.getNames();
        for (String name : names) {
            String value = request.getValue(name);
            header.add(name, value);
        }
        
        return header;
    }
    
    public void onReload() {
        WebApplication oldApplication = application;
        application = application.clone();
        oldApplication.destroy();
    }    
}
