/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.api.wadl.config;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.UriInfo;




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
        resourceConfig.getProperties().put( ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, MyWadlGeneratorConfig.class.getName() );
        
        final WadlGenerator wadlGenerator = WadlGeneratorConfigLoader.loadWadlGeneratorsFromConfig( resourceConfig ).createWadlGenerator();
        assertEquals( MyWadlGenerator.class, wadlGenerator.getClass() );

    }
    
    public void testLoadConfigInstance() {
        
        final WadlGeneratorConfig config = WadlGeneratorConfig.generator( MyWadlGenerator.class ).build();
        
        final ResourceConfig resourceConfig = new DefaultResourceConfig();
        resourceConfig.getProperties().put( ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, config );
        
        final WadlGenerator wadlGenerator = WadlGeneratorConfigLoader.loadWadlGeneratorsFromConfig( resourceConfig ).createWadlGenerator();
        assertTrue( config.createWadlGenerator() instanceof MyWadlGenerator );
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

        
        
        
        public Application createApplication(javax.ws.rs.ext.Providers providers, com.sun.jersey.core.util.FeaturesAndProperties fap, UriInfo requestInfo) {
            return null;
        }

        public Method createMethod( AbstractResource r, AbstractResourceMethod m ) {
            return null;
        }

        public Request createRequest( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        public Param createParam( AbstractResource r,
                AbstractMethod m, Parameter p ) {
            return null;
        }

        public Representation createRequestRepresentation(
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

        public List<Response> createResponses( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        public String getRequiredJaxbContextPath() {
            return null;
        }

        @Override
        public void setEnvironment(Environment env)
        {

        }    
        
        public void init() throws Exception {
            
        }

        public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        }

        
        @Override
        public ExternalGrammarDefinition createExternalGrammar() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void attachTypes(ApplicationDescription egd) {
            throw new UnsupportedOperationException("Not supported yet.");
        }        

        @Override
        public Application createApplication(UriInfo requestInfo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    
    static class MyWadlGeneratorConfig extends WadlGeneratorConfig {

        @Override
        public List<WadlGeneratorDescription> configure() {
            return generator( MyWadlGenerator.class )
            .prop( "foo", "bar" )
            .descriptions();
        }
    }
    
}
