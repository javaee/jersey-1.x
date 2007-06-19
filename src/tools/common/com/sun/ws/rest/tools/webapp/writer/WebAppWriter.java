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

package com.sun.ws.rest.tools.webapp.writer;

import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.StreamSerializer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 *
 * @author Doug Kohlert
 */
public class WebAppWriter {
    protected String servletClassName = "com.sun.ws.rest.impl.container.servlet.ServletAdaptor";
    protected String appName = "REST App";
    protected String urlPattern = "/*";
    protected AnnotationProcessorContext context;
    protected Collection<String> resources;
    
    /** Creates a new instance of WebAppWriter */
    public WebAppWriter() {
    }
    
    public WebAppWriter(String servletClassName, String appName, String urlPattern, AnnotationProcessorContext context) {
        this.servletClassName = servletClassName;
        this.appName = appName;
        this.urlPattern = urlPattern;
        this.context = context;
        this.resources = context.getResourceClasses();
    }   
    
    public void writeTo(OutputStream out) throws IOException  {
        WebApp webApp = TXW.create(WebApp.class, new StreamSerializer(out));
        webApp._namespace("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd", "");
        webApp.version("2.4");
        writeWebApp(webApp);
    }
    
    public void write(PrintWriter writer) throws IOException  {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        writer.print(out.toString());
    }
    
    protected void writeWebApp(WebApp webApp) {
        Servlet servlet = webApp.servlet();
        servlet.servletName(appName);
        servlet.servletClass(servletClassName);
        InitParam param = servlet.initParam();
        param.name("webresourceclass");
        param.value(context.getResourceBeanClassName());
        servlet.loadOnStartup(1);

        ServletMapping mapping = webApp.servletMapping();
        mapping.servletName(appName);
        mapping.urlPattern(urlPattern);
        
        webApp.commit();
        
    }
}
