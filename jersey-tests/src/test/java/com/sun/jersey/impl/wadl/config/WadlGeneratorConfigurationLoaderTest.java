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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.impl.wadl.config;

import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Aug 2, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorConfigurationLoaderTest extends AbstractResourceTester {
    
    public WadlGeneratorConfigurationLoaderTest(String testName) {
        super(testName);
    }
    
    public void testLoadConfigClass() throws URISyntaxException {
        
        final ResourceConfig resourceConfig = new DefaultResourceConfig();
        resourceConfig.getProperties().put( ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, WadlGeneratorConfig.class.getName() );
        
        final String descriptionString = MyWadlGenerator.class.getName() + "[]";
        resourceConfig.getProperties().put( WadlGeneratorConfig.PROPERTY_WADL_GENERATOR_DESCRIPTIONS, descriptionString );
        
        final WadlGenerator wadlGenerator = WadlGeneratorConfigurationLoader.loadWadlGeneratorsFromConfig( resourceConfig );
        assertEquals( MyWadlGenerator.class, wadlGenerator.getClass() );

    }
    
    public void testLoadConfigInstance() {
        
        final WadlGeneratorConfig config = WadlGeneratorConfig.generator( new MyWadlGenerator() ).build();
        
        final ResourceConfig resourceConfig = new DefaultResourceConfig();
        resourceConfig.getProperties().put( ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, config );
        
        final WadlGenerator wadlGenerator = WadlGeneratorConfigurationLoader.loadWadlGeneratorsFromConfig( resourceConfig );
        assertEquals( config.getWadlGenerator(), wadlGenerator );
    }
    
    static class MyWadlGenerator implements WadlGenerator {
        
        private String _foo;

        /**
         * @return the foo
         */
        public String getFoo() {
            return _foo;
        }

        /**
         * @param foo the foo to set
         */
        public void setFoo( String foo ) {
            _foo = foo;
        }

        public Application createApplication() {
            return null;
        }

        public Method createMethod( AbstractResource r, AbstractResourceMethod m ) {
            return null;
        }

        public Request createRequest( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        public Param createRequestParam( AbstractResource r,
                AbstractResourceMethod m, Parameter p ) {
            return null;
        }

        public RepresentationType createRequestRepresentation(
                AbstractResource r, AbstractResourceMethod m,
                MediaType mediaType ) {
            return null;
        }

        public Resource createResource( AbstractResource r, String path ) {
            return null;
        }

        public Resources createResources() {
            return null;
        }

        public Response createResponse( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        public String getRequiredJaxbContextPath() {
            return null;
        }

        public void init() throws Exception {
            
        }

        public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        }
        
    }
    
}
