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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.spi.container.WebApplication;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.core.Response;

/**
 * A {@link HttpHandler} for a {@link WebApplicationImpl}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpHandlerContainer implements HttpHandler {
    
    private WebApplication application;
    
    public HttpHandlerContainer(WebApplication app) throws ContainerException {
        this.application = app;
    }
    
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpServerRequestAdaptor requestAdaptor = 
                new HttpServerRequestAdaptor(
                application.getMessageBodyContext(), 
                httpExchange);
        HttpServerResponseAdaptor responseAdaptor = 
                new HttpServerResponseAdaptor(httpExchange,
                application.getMessageBodyContext(), 
                requestAdaptor);
        
        try {
            application.handleRequest(requestAdaptor, responseAdaptor);
        } catch (ContainerException e) {
            onException(e, responseAdaptor);
        } catch (RuntimeException e) {
            // Unexpected error associated with the runtime
            // This is a bug
            onException(e, responseAdaptor);            
        }
        
        try {
            responseAdaptor.commitAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    private static void onException(Exception e, HttpResponseContext response) {
        // Log the stack trace
        e.printStackTrace();

        // Write out the exception to a string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();

        response.setResponse(Response.serverError().
                entity(sw.toString()).type("text/plain").build());
    }    
}
