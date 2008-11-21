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

package com.sun.jersey.server.impl.container.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class RequestDispatcherWrapper implements RequestDispatcher {
    private final String basePath;
    private final RequestDispatcher d;
    private final Object resource;
    private final Object it;

    public RequestDispatcherWrapper(String basePath, RequestDispatcher d, Object resource, Object it) {
        this.basePath = basePath;
        this.d = d;
        this.resource = resource;
        this.it = it;
    }

    public void forward(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        req.setAttribute("_basePath", basePath);
        req.setAttribute("resource", resource);
        req.setAttribute("it", it);
        req.setAttribute("_request", req);
        req.setAttribute("_response", rsp);
        d.forward(req,rsp);
    }

    public void include(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }
}