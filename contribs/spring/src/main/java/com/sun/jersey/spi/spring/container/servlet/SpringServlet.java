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
package com.sun.jersey.spi.spring.container.servlet;

import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import java.util.Map;
import javax.servlet.ServletException;

/**
 * A servlet or filter for deploying root resource classes with Spring
 * integration.
 * <p>
 * This class extends {@link ServletContainer} and initiates the
 * {@link WebApplication} with a Spring-based {@link IoCComponentProviderFactory},
 * {@link SpringComponentProviderFactory}, such that instances of resource and
 * provider classes declared and managed by Spring can be obtained.
 * <p>
 * Classes of Spring beans declared using XML-based configuration or
 * auto-wire-based confguration will be automatically registered if such
 * classes are root resource classes or provider classes. It is not necessary
 * to provide initialization parameters for declaring classes in the web.xml
 * unless a mixture of Spring-managed and Jersey-managed classes is required.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 */
public class SpringServlet extends ServletContainer {

    private static final long serialVersionUID = 5686655395749077671L;
    
    private static final Logger LOGGER = Logger.getLogger(SpringServlet.class.getName());

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
            WebConfig webConfig) throws ServletException  {
        DefaultResourceConfig rc = new DefaultResourceConfig();
        rc.setPropertiesAndFeatures(props);
        return rc;
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        try {
            final WebApplicationContext springWebContext = WebApplicationContextUtils.
                    getRequiredWebApplicationContext(getServletContext());
            final ConfigurableApplicationContext springContext =
                    (ConfigurableApplicationContext)springWebContext;

            wa.initiate(rc, new SpringComponentProviderFactory(rc, springContext));
        } catch( RuntimeException e ) {
            LOGGER.log(Level.SEVERE, "Exception occurred when intialization", e);
            throw e;
        }
    }
}