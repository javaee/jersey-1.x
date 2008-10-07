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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 * A servlet container for deploying root resource classes with Spring
 * integration.
 * <p>
 * This servlet extends {@link ServletContainer} and initiates the
 * {@link WebApplication} with a Spring-based {@link ComponentProvider},
 * {@link SpringComponentProvider}, such that resource and provider classes
 * can be registered Spring-based beans using XML-based registration or
 * auto-wire-based registration.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 */
public class SpringServlet extends ServletContainer {

    private static final long serialVersionUID = 5686655395749077671L;
    
    private static final Logger LOGGER = Logger.getLogger(SpringServlet.class.getName());

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        try {
            final WebApplicationContext springContext = WebApplicationContextUtils.
                    getRequiredWebApplicationContext(getServletContext());
            
            if ( rc.getProperty( ResourceConfig.PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS ) == null
                    && springComponentAnnotationAvailable() ) {
                rc.getProperties().put( ResourceConfig.PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS,
                        SpringResourceProvider.class );
            }
            
            wa.initiate(rc, new SpringComponentProvider((ConfigurableApplicationContext) springContext));
        } catch( RuntimeException e ) {
            LOGGER.log(Level.SEVERE, "Exception occurred when intialization", e);
            throw e;
        }
    }
    
    private boolean springComponentAnnotationAvailable() {
        try {
            Class.forName("org.springframework.stereotype.Component");
            LOGGER.info( "The spring Component annotation is present: using spring >= 2.5" );
            return true;
        } catch ( ClassNotFoundException e ) {
            LOGGER.info( "The spring Component annotation is not present: using spring < 2.5" );
            return false;
        }
    }
}