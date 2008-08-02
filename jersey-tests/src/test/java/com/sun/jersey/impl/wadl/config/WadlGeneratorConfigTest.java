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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
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
public class WadlGeneratorConfigTest extends TestCase {
    
    public WadlGeneratorConfigTest(String testName) {
        super(testName);
    }
    
    public void testBuildWadlGeneratorFromGenerators() {
        final MyWadlGenerator generator = new MyWadlGenerator();
        final MyWadlGenerator2 generator2 = new MyWadlGenerator2();
        WadlGeneratorConfig config = WadlGeneratorConfig
            .generator( generator )
            .generator( generator2 )
            .build();
        
        WadlGenerator wadlGenerator = config.getWadlGenerator();

        assertEquals( MyWadlGenerator2.class, wadlGenerator.getClass() );
        assertEquals( MyWadlGenerator.class, ( (MyWadlGenerator2)wadlGenerator ).getDelegate().getClass() );
    }
    
    public void testBuildWadlGeneratorFromDescriptions() {
        final String propValue = "bar";
        WadlGeneratorConfig config = WadlGeneratorConfig
            .generator( MyWadlGenerator.class.getName() )
            .prop( "foo", propValue )
            .add().build();
        WadlGenerator wadlGenerator = config.getWadlGenerator();
        assertEquals( MyWadlGenerator.class, wadlGenerator.getClass() );
        assertEquals( ((MyWadlGenerator)wadlGenerator).getFoo(), propValue );

        final String propValue2 = "baz";
        config = WadlGeneratorConfig
            .generator( MyWadlGenerator.class.getName() )
            .prop( "foo", propValue )
            .add()
            .generator( MyWadlGenerator2.class.getName() )
            .prop( "bar", propValue2 )
            .add()
            .build();
        wadlGenerator = config.getWadlGenerator();
        assertEquals( MyWadlGenerator2.class, wadlGenerator.getClass() );
        final MyWadlGenerator2 wadlGenerator2 = (MyWadlGenerator2)wadlGenerator;
        assertEquals( wadlGenerator2.getBar(), propValue2 );
        
        assertEquals( MyWadlGenerator.class, wadlGenerator2.getDelegate().getClass() );
        assertEquals( ((MyWadlGenerator)wadlGenerator2.getDelegate()).getFoo(), propValue );
        
    }
    
    public void testLoadWadlGeneratorDescriptionsFromConfigString() {
        
        final String descriptionString = "com.sun.jersey.impl.wadl.generators.WadlGeneratorApplicationDoc" +
        		    "[applicationDocsFile=classpath:/application-doc.xml];\n" +
        		"com.sun.jersey.impl.wadl.generators.WadlGeneratorGrammarsSupport" +
        		    "[grammarsFile=classpath:/application-grammars.xml," +
        		    "foo=bar]";
        
        final Map<String, Object> props = new HashMap<String, Object>();
        props.put( WadlGeneratorConfig.PROPERTY_WADL_GENERATOR_DESCRIPTIONS, descriptionString );
        
        final List<WadlGeneratorDescription> descriptions = WadlGeneratorConfig.loadWadlGeneratorDescriptions( props );
        assertEquals( 2, descriptions.size() );
        
        WadlGeneratorDescription description = descriptions.get( 0 );
        assertEquals( "com.sun.jersey.impl.wadl.generators.WadlGeneratorApplicationDoc", description.getClassName() );
        assertEquals( 1, description.getProperties().size() );
        assertEquals( description.getProperties().getProperty( "applicationDocsFile" ), "classpath:/application-doc.xml" );

        description = descriptions.get( 1 );
        assertEquals( "com.sun.jersey.impl.wadl.generators.WadlGeneratorGrammarsSupport", description.getClassName() );
        assertEquals( 2, description.getProperties().size() );
        assertEquals( description.getProperties().getProperty( "grammarsFile" ), "classpath:/application-grammars.xml" );
        assertEquals( description.getProperties().getProperty( "foo" ), "bar" );
        
    }
    
    public void testLoadWadlGeneratorDescriptionsFromConfigStringWithoutProperties() {
        
        final String descriptionString = MyWadlGenerator.class.getName() + "[];\n" +
                    MyWadlGenerator2.class.getName() + "[]";
        
        final Map<String, Object> props = new HashMap<String, Object>();
        props.put( WadlGeneratorConfig.PROPERTY_WADL_GENERATOR_DESCRIPTIONS, descriptionString );
        
        final List<WadlGeneratorDescription> descriptions = WadlGeneratorConfig.loadWadlGeneratorDescriptions( props );
        assertEquals( 2, descriptions.size() );
        
        WadlGeneratorDescription description = descriptions.get( 0 );
        assertEquals( MyWadlGenerator.class.getName(), description.getClassName() );
        assertEquals( 0, description.getProperties().size() );

        description = descriptions.get( 1 );
        assertEquals( MyWadlGenerator2.class.getName(), description.getClassName() );
        assertEquals( 0, description.getProperties().size() );
        
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

        @Override
        public Application createApplication() {
            return null;
        }

        @Override
        public Method createMethod( AbstractResource r, AbstractResourceMethod m ) {
            return null;
        }

        @Override
        public Request createRequest( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        @Override
        public Param createRequestParam( AbstractResource r,
                AbstractResourceMethod m, Parameter p ) {
            return null;
        }

        @Override
        public RepresentationType createRequestRepresentation(
                AbstractResource r, AbstractResourceMethod m,
                MediaType mediaType ) {
            return null;
        }

        @Override
        public Resource createResource( AbstractResource r, String path ) {
            return null;
        }

        @Override
        public Resources createResources() {
            return null;
        }

        @Override
        public Response createResponse( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        @Override
        public String getRequiredJaxbContextPath() {
            return null;
        }

        @Override
        public void init() throws Exception {
            
        }

        @Override
        public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        }
        
    }
    
    static class MyWadlGenerator2 implements WadlGenerator {
        
        private String _bar;
        private WadlGenerator _delegate;

        @Override
        public void init() throws Exception {
            
        }

        @Override
        public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
            _delegate = delegate;
        }

        /**
         * @return the delegate
         */
        public WadlGenerator getDelegate() {
            return _delegate;
        }

        /**
         * @return the foo
         */
        public String getBar() {
            return _bar;
        }

        /**
         * @param foo the foo to set
         */
        public void setBar( String foo ) {
            _bar = foo;
        }

        @Override
        public Application createApplication() {
            return null;
        }

        @Override
        public Method createMethod( AbstractResource r, AbstractResourceMethod m ) {
            return null;
        }

        @Override
        public Request createRequest( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        @Override
        public Param createRequestParam( AbstractResource r,
                AbstractResourceMethod m, Parameter p ) {
            return null;
        }

        @Override
        public RepresentationType createRequestRepresentation(
                AbstractResource r, AbstractResourceMethod m,
                MediaType mediaType ) {
            return null;
        }

        @Override
        public Resource createResource( AbstractResource r, String path ) {
            return null;
        }

        @Override
        public Resources createResources() {
            return null;
        }

        @Override
        public Response createResponse( AbstractResource r,
                AbstractResourceMethod m ) {
            return null;
        }

        @Override
        public String getRequiredJaxbContextPath() {
            return null;
        }
        
    }
    
}
