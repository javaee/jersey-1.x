/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.wadl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.research.ws.wadl.Application;

/**
 * This mojo generates a wadl file, without the need of a running webapp.<br />
 * Created on: Jun 18, 2008<br />
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 * 
 * @goal generate
 * @phase compile
 */
public class GenerateWadlMojo extends AbstractMojoProjectClasspathSupport {

    /**
     * Location of the wadl file to create.
     * 
     * @parameter property="wadlFile" expression="${project.build.directory}/application.wadl"
     * @required
     */
    private File _wadlFile;
    
    /**
     * Specifies, if the generated wadl file shall contain formatted xml or not.
     * The default value is <code>true</code>.
     * 
     * @parameter property="formatWadlFile"
     */
    private boolean _formatWadlFile = true;

    /**
     * The base-uri to use.
     * 
     * @parameter property="baseUri"
     * @required
     */
    private String _baseUri;
    
    /**
     * An array of packages that is searched for resource classes.
     * 
     * @parameter property="packagesResourceConfig"
     * @required
     */
    private String[] _packagesResourceConfig;
    
    /**
     * An array of packages that is searched for resource classes.
     * 
     * @parameter property="wadlGenerators"
     * @required
     */
    private List<WadlGeneratorDescription> _wadlGenerators;
    
    public void executeWithClasspath( List<String> classpathElements ) throws MojoExecutionException {
        if ( _packagesResourceConfig == null || _packagesResourceConfig.length == 0 ) {
            throw new MojoExecutionException( "The packagesResourceConfig attribute is required but not defined." );
        }
        if (_wadlFile == null) {
            throw new MojoExecutionException( "The wadlFile attribute is required but not defined." );
        }

        if (_baseUri == null || _baseUri.length() == 0) {
            throw new MojoExecutionException( "The baseUri attribute is required but not defined." );
        }
        
        try {
            
            com.sun.jersey.server.wadl.WadlGenerator wadlGenerator = new WadlGeneratorImpl();
            if ( _wadlGenerators != null ) {
                for ( WadlGeneratorDescription wadlGeneratorDescription : _wadlGenerators ) {
                    wadlGenerator = loadWadlGenerator( wadlGeneratorDescription, wadlGenerator );
                }
            }
            wadlGenerator.init();
            
            final Application a = createApplication( _packagesResourceConfig, wadlGenerator );
            a.getResources().setBase( _baseUri );
            
            final JAXBContext c = JAXBContext.newInstance( wadlGenerator.getRequiredJaxbContextPath(), 
                    Thread.currentThread().getContextClassLoader() );
            final Marshaller m = c.createMarshaller();
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, _formatWadlFile );
            // m.setProperty( "com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl() );
            // m.setProperty( "com.sun.xml.bind.characterEscapeHandler", new CustomCharacterEscapeHandler() );
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(_wadlFile));
            
            // get an Apache XMLSerializer configured to generate CDATA
            final XMLSerializer serializer = getXMLSerializer( out );
            
            m.marshal( a, serializer );
            out.close();
            
            getLog().info( "Wrote " + _wadlFile );
            
        } catch( MojoExecutionException e ) {
            throw e;
        } catch (Exception e) {
            getLog().error( e );
            throw new MojoExecutionException( "Could not write wadl file", e );            
        }
    }
    
    private XMLSerializer getXMLSerializer(OutputStream out) throws FileNotFoundException {
        // configure an OutputFormat to handle CDATA
        OutputFormat of = new OutputFormat();

        // specify which of your elements you want to be handled as CDATA.
        // The use of the '^' between the namespaceURI and the localname
        // seems to be an implementation detail of the xerces code.
    // When processing xml that doesn't use namespaces, simply omit the
    // namespace prefix as shown in the third CDataElement below.
        of.setCDataElements(
                new String[] { "http://research.sun.com/wadl/2006/10^doc",   // <ns1:foo>
                       "ns2^doc",   // <ns2:bar>
                       "^doc"
                       /*,
                       "ns2:doc",
                       "doc"*/ } );   // <baz>

        // set any other options you'd like
        of.setPreserveSpace( true );
        of.setIndenting( true );
        // of.setLineWidth( 120 );
        // of.setNonEscapingElements( new String[] { "http://www.w3.org/1999/xhtml^br", "http://www.w3.org/1999/xhtml^br" } );

        // create the serializer
        XMLSerializer serializer = new XMLSerializer(of);
        
        serializer.setOutputByteStream( out );

        return serializer;
    }

    private WadlGenerator loadWadlGenerator(
            WadlGeneratorDescription wadlGeneratorDescription,
            com.sun.jersey.server.wadl.WadlGenerator wadlGeneratorDelegate ) throws Exception {
        getLog().info( "Loading wadlGenerator " + wadlGeneratorDescription.getClassName() );
        final Class<?> clazz = Class.forName( wadlGeneratorDescription.getClassName(), true, Thread.currentThread().getContextClassLoader() );
        final WadlGenerator generator = clazz.asSubclass( WadlGenerator.class ).newInstance();
        generator.setWadlGeneratorDelegate( wadlGeneratorDelegate );
        if ( wadlGeneratorDescription.getProperties() != null
                && !wadlGeneratorDescription.getProperties().isEmpty() ) {
            for ( Entry<Object, Object> entry : wadlGeneratorDescription.getProperties().entrySet() ) {
                setProperty( generator, entry.getKey().toString(), entry.getValue() );
            }
        }
        return generator;
    }

    private void setProperty( final Object object, final String propertyName, final Object propertyValue )
            throws Exception {
        final String methodName = "set" + propertyName.substring( 0, 1 ).toUpperCase() + propertyName.substring( 1 );
        final Method method = getMethodByName( methodName, object.getClass() );
        if ( method.getParameterTypes().length != 1 ) {
            throw new RuntimeException( "Method " + methodName + " is no setter, it does not expect exactly one parameter, but " + method.getParameterTypes().length );
        }
        final Class<?> paramClazz = method.getParameterTypes()[0];
        if ( paramClazz == propertyValue.getClass() ) {
            method.invoke( object, propertyValue );
        }
        else {
            /* does the param class provide a constructor for string?
             */
            final Constructor<?> paramTypeConstructor = getMatchingConstructor( paramClazz, propertyValue );
            if ( paramTypeConstructor != null ) {
                final Object typedPropertyValue;
                try {
                    typedPropertyValue = paramTypeConstructor.newInstance( propertyValue );
                } catch( Exception e ) {
                    throw new Exception( "Could not create instance of configured property " + propertyName +
                            " from value " + propertyValue + ", using the constructor " + paramTypeConstructor, e );
                }
                method.invoke( object, typedPropertyValue );
            }
            else {
                throw new RuntimeException( "The property '" + propertyName + "' could not be set" +
                		" because the expected parameter is neither of type " + propertyValue.getClass() +
                		" nor of any type that provides a constructor expecting a " + propertyValue.getClass() + "." +
                		" The expected parameter is of type " + paramClazz.getName() );
            }
        }
    }

    private Constructor<?> getMatchingConstructor( final Class<?> paramClazz,
            final Object propertyValue ) {
        final Constructor<?>[] constructors = paramClazz.getConstructors();
        for ( Constructor<?> constructor : constructors ) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if ( parameterTypes.length == 1
                    && constructor.getParameterTypes()[0] == propertyValue.getClass() ) {
                return constructor;
            }
        }
        return null;
    }

    private Method getMethodByName( final String methodName, final Class<?> clazz ) {
        for ( Method method : clazz.getMethods() ) {
            if ( method.getName().equals( methodName ) ) {
                return method;
            }
        }
        throw new RuntimeException( "Method '" + methodName + "' not found for class " + clazz.getName() );
    }

    private Application createApplication( String[] paths, WadlGenerator wadlGenerator ) throws MojoExecutionException {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put( PackagesResourceConfig.PROPERTY_PACKAGES, paths );
        final ResourceConfig rc = new PackagesResourceConfig( map );
        final Set<AbstractResource> s = new HashSet<AbstractResource>();
        for (Class<?> c : rc.getRootResourceClasses()) {
            getLog().debug( "Adding class " + c.getName() );
            s.add( IntrospectionModeller.createResource(c) );
        }
        return new WadlBuilder( wadlGenerator ).generate( s );   
    }

    /**
     * @param wadlFile the wadlFile to set
     * @author Martin Grotzke
     */
    public void setWadlFile( File wadlFile ) {
        _wadlFile = wadlFile;
    }

    /**
     * @param baseUri the baseUri to set
     * @author Martin Grotzke
     */
    public void setBaseUri( String baseUri ) {
        _baseUri = baseUri;
    }

    /**
     * @param packagesResourceConfig the packagesResourceConfig to set
     * @author Martin Grotzke
     */
    public void setPackagesResourceConfig( String[] packagesResourceConfig ) {
        _packagesResourceConfig = packagesResourceConfig;
    }

    /**
     * @param formatWadlFile the formatWadlFile to set
     * @author Martin Grotzke
     */
    public void setFormatWadlFile( boolean formatWadlFile ) {
        _formatWadlFile = formatWadlFile;
    }

    /**
     * @param wadlGenerators the wadlGenerators to set
     * @author Martin Grotzke
     */
    public void setWadlGenerators( List<WadlGeneratorDescription> wadlGenerators ) {
        _wadlGenerators = wadlGenerators;
    }
    
}
