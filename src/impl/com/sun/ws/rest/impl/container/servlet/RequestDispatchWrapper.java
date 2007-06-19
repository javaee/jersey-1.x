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

package com.sun.ws.rest.impl.container.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class RequestDispatcherWrapper implements RequestDispatcher {
    private final RequestDispatcher d;
    private final Object it;

    public RequestDispatcherWrapper(RequestDispatcher d, Object it) {
        this.d = d;
        this.it = it;
    }

    public void forward(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        req.setAttribute("it", it);
        req.setAttribute("_request", req);
        req.setAttribute("_response", rsp);
        d.forward(req,rsp);
    }

    public void include(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }
}