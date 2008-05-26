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

package com.sun.jersey.impl.container.grizzly;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.spi.container.WebApplication;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.jersey.spi.container.ContainerListener;
import java.io.IOException;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class GrizzlyContainer extends GrizzlyAdapter implements ContainerListener {
    
    private WebApplication application;
    
    public GrizzlyContainer(WebApplication app) throws ContainerException {
        this.application = app;
    }

    public void service(GrizzlyRequest request, GrizzlyResponse response) {
        GrizzlyResponseAdaptor responseAdaptor = null;
        WebApplication _application = application;
        
        try {
            GrizzlyRequestAdaptor requestAdaptor = 
                    new GrizzlyRequestAdaptor(_application.getMessageBodyContext(), 
                    request);
            responseAdaptor = 
                    new GrizzlyResponseAdaptor(response, 
                    _application.getMessageBodyContext(), 
                    requestAdaptor);
        
            _application.handleRequest(requestAdaptor, responseAdaptor);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        } catch (ContainerException e) {
            onException(e, responseAdaptor);
        } catch (RuntimeException e) {
            // Unexpected error associated with the runtime
            // This is a bug
            onException(e, responseAdaptor);
        }
        
        try {
            responseAdaptor.commitAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void afterService(GrizzlyRequest request, GrizzlyResponse response) 
            throws Exception {
    }

    
    private static void onException(Exception e, HttpResponseContext response) {
        // Log the stack trace
        e.printStackTrace();

        // Write out the exception to a string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();

        response.setResponse(javax.ws.rs.core.Response.serverError().
                entity(sw.toString()).type("text/plain").build());
    }

    // ContainerListener
    
    public void onReload() {
        application = application.clone();
    }    
}
