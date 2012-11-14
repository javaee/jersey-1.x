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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.api.wadl.config;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.core.UriInfo;




/**
 * Test the {@link WadlGeneratorLoader}.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorLoaderTest extends AbstractResourceTester {
    
    public WadlGeneratorLoaderTest(String testName) {
        super(testName);
    }
    
    public void testLoadFileFromClasspathRelative() throws Exception {
        
        final Properties props = new Properties();
        props.put( "testFile", "classpath:testfile.xml" );
        final WadlGeneratorDescription description = new WadlGeneratorDescription( MyWadlGenerator2.class, props );
        
        final WadlGenerator wadlGenerator = WadlGeneratorLoader.loadWadlGeneratorDescriptions( description );
        assertEquals( MyWadlGenerator2.class, wadlGenerator.getClass() );

        final URL resource = getClass().getResource( "testfile.xml" );
        assertEquals( new File( resource.toURI() ).getAbsolutePath(), ((MyWadlGenerator2)wadlGenerator).getTestFile().getAbsolutePath() );
        
    }
    
    public void testLoadFileFromClasspathAbsolute() throws Exception {
        
        final Properties props = new Properties();
        final String path = "classpath:/" + getClass().getPackage().getName().replaceAll( "\\.", "/" ) + "/testfile.xml";
        props.put( "testFile", path );
        final WadlGeneratorDescription description = new WadlGeneratorDescription( MyWadlGenerator2.class, props );
        
        final WadlGenerator wadlGenerator = WadlGeneratorLoader.loadWadlGeneratorDescriptions( description );
        assertEquals( MyWadlGenerator2.class, wadlGenerator.getClass() );

        final URL resource = getClass().getResource( "testfile.xml" );
        assertEquals( new File( resource.toURI() ).getAbsolutePath(), ((MyWadlGenerator2)wadlGenerator).getTestFile().getAbsolutePath() );
        
    }
    
    public void testLoadFileFromAbsolutePath() throws Exception {
        
        final URL resource = getClass().getResource( "testfile.xml" );
        
        final Properties props = new Properties();
        final String path = new File( resource.toURI() ).getAbsolutePath();
        props.put( "testFile", path );
        final WadlGeneratorDescription description = new WadlGeneratorDescription( MyWadlGenerator2.class, props );
        
        final WadlGenerator wadlGenerator = WadlGeneratorLoader.loadWadlGeneratorDescriptions( description );
        assertEquals( MyWadlGenerator2.class, wadlGenerator.getClass() );

        assertEquals( new File( resource.toURI() ).getAbsolutePath(), ((MyWadlGenerator2)wadlGenerator).getTestFile().getAbsolutePath() );
        
    }
    
    public void testLoadStream() throws Exception {
        
        final Properties props = new Properties();
        final String path = getClass().getPackage().getName().replaceAll( "\\.", "/" ) + "/testfile.xml";
        props.put( "testStream", path );
        final WadlGeneratorDescription description = new WadlGeneratorDescription( MyWadlGenerator2.class, props );
        
        final WadlGenerator wadlGenerator = WadlGeneratorLoader.loadWadlGeneratorDescriptions( description );
        assertEquals( MyWadlGenerator2.class, wadlGenerator.getClass() );

        final URL resource = getClass().getResource( "testfile.xml" );
        assertEquals( new File( resource.toURI() ).length(), ((MyWadlGenerator2)wadlGenerator).getTestStreamContent().length() );
        
    }
    
    static class MyWadlGenerator2 implements WadlGenerator {


        private File _testFile;
        private InputStream _testStream;
        private File _testStreamContent;
        private WadlGenerator _delegate;

        /**
         * @param testFile the testFile to set
         */
        public void setTestFile( File testFile ) {
            _testFile = testFile;
        }

        public void setTestStream( InputStream testStream ) {
            _testStream = testStream;
        }

        public File getTestFile() {
            return _testFile;
        }
        
        public File getTestStreamContent() {
            /*
            try {
                System.out.println( "listing file " + _testFileContent.getName() );
                BufferedReader in = new BufferedReader( new FileReader( _testFileContent ) );
                String line = null;
                while ( (line = in.readLine()) != null ) {
                    System.out.println( line );
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            */
            return _testStreamContent;
        }

        public void init() throws Exception {
            if ( _testStream != null ) {
                _testStreamContent = File.createTempFile( "testfile-" + getClass().getSimpleName(), null );
                OutputStream to = null;
                try {
                    to = new FileOutputStream(_testStreamContent);
                    byte[] buffer = new byte[4096];
                    int bytes_read;
                    while((bytes_read = _testStream.read(buffer)) != -1) {
                        to.write(buffer, 0, bytes_read);
                    }
                }
                // Always close the streams, even if exceptions were thrown
                finally {
                    if (to != null) try { to.close(); } catch (IOException e) { ; }
                }
            }
        }

        public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
            _delegate = delegate;
        }

        /**
         * @return the delegate
         */
        public WadlGenerator getDelegate() {
            return _delegate;
        }

        public Application createApplication(UriInfo requestInfo) {
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
        public ExternalGrammarDefinition createExternalGrammar() {
            return _delegate.createExternalGrammar();
        }

        @Override
        public void attachTypes(ApplicationDescription egd) {
            _delegate.attachTypes(egd);
        }        
    }
    
}
