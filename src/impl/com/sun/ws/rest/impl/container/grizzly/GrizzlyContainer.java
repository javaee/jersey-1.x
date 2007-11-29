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

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.spi.container.WebApplication;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.sun.grizzly.tcp.ActionCode;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class GrizzlyContainer implements Adapter {
    
    private WebApplication application;
    
    public GrizzlyContainer(WebApplication app) throws ContainerException {
        this.application = app;
    }

    public void service(Request request, Response response) throws Exception {
        GrizzlyRequestAdaptor requestAdaptor = new GrizzlyRequestAdaptor(request);
        GrizzlyResponseAdaptor responseAdaptor = new GrizzlyResponseAdaptor(response, requestAdaptor);
        
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
            request.action( ActionCode.ACTION_POST_REQUEST , null);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        } finally {
            response.finish();
        }
    }
    
    public void afterService(Request request, Response response) throws Exception {
        request.recycle();
        response.recycle();          
    }

    public void fireAdapterEvent(String string, Object object) {
        // Not used.
    }
    
    private static void onException(Exception e, HttpResponseContext response) {
        // Log the stack trace
        e.printStackTrace();

        // Write out the exception to a string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();

        response.setResponse(ResponseBuilderImpl.serverError().
                entity(sw.toString()).type("text/plain").build());
    }
}
