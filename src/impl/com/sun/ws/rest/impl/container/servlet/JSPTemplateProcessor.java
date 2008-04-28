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

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.spi.template.TemplateProcessor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * A JSP template processor.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class JSPTemplateProcessor implements TemplateProcessor {
    @Context ServletContext servletContext;

    public JSPTemplateProcessor() {
        Class<?> c = ServletContext.class;
    }
    
    public String resolve(String path) {
        if (servletContext == null)
            return null;
        
        if (!path.endsWith(".jsp"))
            path = path + ".jsp";

        try {
            if (servletContext.getResource(path) == null) {
                // TODO log
                return null;
            }
        } catch (MalformedURLException ex) {
            // TODO log
            return null;
        }
        
        return path;        
    }

    @Context HttpContext hca;
    
    public void writeTo(String resolvedPath, Object model, OutputStream out) throws IOException {
        HttpResponseContext response = hca.getResponse();
        ((HttpResponseAdaptor)response).forwardTo(resolvedPath, model);
    }
}