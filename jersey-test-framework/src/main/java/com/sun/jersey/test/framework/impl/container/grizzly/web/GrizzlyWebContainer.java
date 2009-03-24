/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.test.framework.impl.container.grizzly.web;

import com.sun.jersey.test.framework.impl.BasicLightWeightContainer;
import com.sun.jersey.test.framework.*;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.test.framework.impl.BasicServletContainer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Servlet;

/**
 * The class provides methods for creating, starting and stopping an instance of
 * the embedded Grizzly server.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class GrizzlyWebContainer implements BasicServletContainer {

    /**
     * Map of init params to be passed to the servlet.
     */
    private Map<String, String> initParams;

    private Map<String, String> contextParams;

    private Class servletClass;

    private String contextListenerClassName;

    private String servletPath;

    private String contextPath;

    /**
     * Handle to the Grizzly container.
     */
    private SelectorThread threadSelector;

    /**
     * Base uri for the resources.
     */
    private URI BASE_URI;

    private GrizzlyWebServer ws;

    private static final Logger logger = Logger.getLogger(GrizzlyWebContainer.class
            .getName());

    private static int index=0;

    /**
     * Default constructor.
     */
    public GrizzlyWebContainer() {
        initParams = new HashMap<String,String>();
        contextParams = new HashMap<String, String>();
    }

    /**
     * Start grizzly and deploy endpoint.
     * @throws java.lang.Exception
     */
    public void start() throws Exception {
        // Create an instance of grizzly web container
        logger.log(Level.INFO, "Starting grizzly...");
        ws = new GrizzlyWebServer(BASE_URI.getPort());
        ServletAdapter sa = new ServletAdapter();
        index++;
        sa.setRootFolder("."+index);
        Servlet servletInstance = (Servlet)servletClass.newInstance();
        sa.setServletInstance(servletInstance);
        sa.addServletListener(contextListenerClassName);
        for(String contextParamName : contextParams.keySet()) {
            sa.addContextParameter(contextParamName, contextParams.get(contextParamName));
        }
        for(String initParamName : initParams.keySet()) {
            sa.addInitParameter(initParamName, initParams.get(initParamName));
        }
        if(contextPath != null && contextPath.length() > 0) {
            if( !contextPath.startsWith("/") ) {
                contextPath = "/" + contextPath;
            }
            sa.setContextPath(contextPath);
        }
        if(servletPath != null && servletPath.length() > 0) {
            if( !servletPath.startsWith("/") ) {
                servletPath = "/" + servletPath;
            }
            sa.setServletPath(servletPath);
        }
        String[] mapping = null;
        ws.addGrizzlyAdapter(sa, mapping);
        //ws.addGrizzlyAdapter(sa);
        ws.start();                
    }

    /**
     * Stop grizzly server.
     * @throws java.lang.Exception
     */
    public void stop() throws Exception {
        // stop the grizzly thread selector instance
            //threadSelector.stopEndpoint();
        ws.stop();
    }

    /**
     * Set base uri.
     * @param baseUri
     */
    public void setBaseUri(URI baseUri) {
        BASE_URI = baseUri;
    }

    /**
     * Set the servlet init params.
     * @param initParams
     */
    public void setInitParams(Map<String, String> initParams) {
        this.initParams.putAll(initParams);
    }
    
    public void setHttpListenerPort(int httpPort) {
        //do nothing
    }

    public void setContextParams(Map<String, String> contextParams) {
        if (contextParams != null) {
            this.contextParams.putAll(contextParams);
        }
    }

    public void setServletClass(Class servletClass) {
        this.servletClass = (servletClass != null) ? servletClass : ServletContainer.class;
    }

    public void setServletListener(String servletListenerClass) {
        if (servletListenerClass != null && servletListenerClass.length() > 0) {
            this.contextListenerClassName = servletListenerClass;
        }
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
