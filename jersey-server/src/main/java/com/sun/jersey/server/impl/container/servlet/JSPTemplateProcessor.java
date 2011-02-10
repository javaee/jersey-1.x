/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.server.impl.container.servlet;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.template.ViewProcessor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

/**
 * A JSP template processor.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class JSPTemplateProcessor implements ViewProcessor<String> {
    private @Context HttpContext hc;
    
    private @Context ServletContext servletContext;
    
    private @Context ThreadLocal<HttpServletRequest> requestInvoker;
    
    private @Context ThreadLocal<HttpServletResponse> responseInvoker;

    private final String basePath;
    
    public JSPTemplateProcessor(
            @Context ResourceConfig resourceConfig) {

        String path = (String)resourceConfig.getProperties().get(
                ServletContainer.JSP_TEMPLATES_BASE_PATH);
        if (path == null)
            this.basePath = "";
        else if (path.charAt(0) == '/') {
            this.basePath = path;
        } else {
            this.basePath = "/" + path;
        }
    }
    
    public String resolve(String path) {
        if (servletContext == null)
            return null;

        if (basePath != "")
            path = basePath + path;
        
        try {
            if (servletContext.getResource(path) != null) {
                return path;
            }
                
            if (!path.endsWith(".jsp")) {
                path = path + ".jsp";
                if (servletContext.getResource(path) != null) {
                    return path;
                }
            }            
        } catch (MalformedURLException ex) {
            // TODO log
        }
        
        return null;
    }

    public void writeTo(String resolvedPath, Viewable viewable, OutputStream out) throws IOException {
        if (hc.isTracingEnabled()) {
            hc.trace(String.format("forwarding view to JSP page: \"%s\", it = %s",
                    resolvedPath,
                    ReflectionHelper.objectToString(viewable.getModel())));
        }

        // Commit the status and headers to the HttpServletResponse
        out.flush();

        RequestDispatcher d = servletContext.getRequestDispatcher(resolvedPath);
        if (d == null) {
            throw new ContainerException("No request dispatcher for: " + resolvedPath);
        }

        d = new RequestDispatcherWrapper(d, basePath, hc, viewable);

        try {
            d.forward(requestInvoker.get(), responseInvoker.get());
        } catch (Exception e) {
            throw new ContainerException(e);
        }
    }
}