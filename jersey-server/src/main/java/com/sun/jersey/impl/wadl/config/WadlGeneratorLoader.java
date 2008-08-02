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
package com.sun.jersey.impl.wadl.config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.jersey.impl.wadl.WadlGeneratorImpl;

/**
 * Loads {@link WadlGenerator}s from a provided list of {@link WadlGeneratorDescription}s.<br/>
 * The properties of the {@link WadlGeneratorDescription}s can refer to {@link WadlGenerator} properties
 * of these types:
 * <ul>
 * <li>exact match: if the WadlGenerator property is of type <code>org.example.Foo</code> and the
 * property value provided by the {@link WadlGeneratorDescription} is of type <code>org.example.Foo</code></li>
 * <li>java.lang.File: The property value can contain the prefix <em>classpath:</em> to denote, that the
 * path to the file is relative to the classpath. In this case, the property value is stripped by 
 * the prefix <em>classpath:</em> and the java.lang.File is created via
 * <pre><code>new File( generator.getClass().getResource( strippedFilename ).toURI() )</code></pre></li>
 * <li>Types that provide a constructor for the provided type (mostly java.lang.String)</li>
 * </ul>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
class WadlGeneratorLoader {

    private static final Logger LOGGER = Logger.getLogger( WadlGeneratorLoader.class.getName() );

    static WadlGenerator loadWadlGenerators(
            List<WadlGenerator> wadlGenerators ) throws Exception {
        WadlGenerator wadlGenerator = new WadlGeneratorImpl();
        if ( wadlGenerators != null && !wadlGenerators.isEmpty() ) {
            for ( WadlGenerator generator : wadlGenerators ) {
                generator.setWadlGeneratorDelegate( wadlGenerator );
                wadlGenerator = generator;
            }
        }
        wadlGenerator.init();
        return wadlGenerator;
    }
    
    static WadlGenerator loadWadlGeneratorDescriptions( WadlGeneratorDescription ... wadlGeneratorDescriptions ) throws Exception {
        final List<WadlGeneratorDescription> list = wadlGeneratorDescriptions != null ? Arrays.asList( wadlGeneratorDescriptions ) : null;
        return loadWadlGeneratorDescriptions( list );
    }

    static WadlGenerator loadWadlGeneratorDescriptions( List<WadlGeneratorDescription> wadlGeneratorDescriptions ) throws Exception {
        WadlGenerator wadlGenerator = new WadlGeneratorImpl();
        if ( wadlGeneratorDescriptions != null && !wadlGeneratorDescriptions.isEmpty() ) {
            for ( WadlGeneratorDescription wadlGeneratorDescription : wadlGeneratorDescriptions ) {
                wadlGenerator = loadWadlGenerator( wadlGeneratorDescription, wadlGenerator );
            }
        }
        wadlGenerator.init();
        return wadlGenerator;
        
    }

    private static WadlGenerator loadWadlGenerator(
            WadlGeneratorDescription wadlGeneratorDescription,
            com.sun.jersey.impl.wadl.WadlGenerator wadlGeneratorDelegate ) throws Exception {
        LOGGER.info( "Loading wadlGenerator " + wadlGeneratorDescription.getClassName() );
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

    private static void setProperty( final Object generator, final String propertyName, final Object propertyValue )
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, URISyntaxException {
        final String methodName = "set" + propertyName.substring( 0, 1 ).toUpperCase() + propertyName.substring( 1 );
        final Method method = getMethodByName( methodName, generator.getClass() );
        if ( method.getParameterTypes().length != 1 ) {
            throw new RuntimeException( "Method " + methodName + " is no setter, it does not expect exactly one parameter, but " + method.getParameterTypes().length );
        }
        final Class<?> paramClazz = method.getParameterTypes()[0];
        if ( paramClazz == propertyValue.getClass() ) {
            method.invoke( generator, propertyValue );
        }
        else if ( File.class.equals( paramClazz ) && propertyValue instanceof String ) {
            final String filename = propertyValue.toString();
            if ( filename.startsWith( "classpath:" ) ) {
                final String strippedFilename = filename.substring( "classpath:".length() );
                final URL resource = generator.getClass().getResource( strippedFilename );
                if ( resource == null ) {
                    throw new RuntimeException( "The file '" + strippedFilename + "' does not exist in the classpath." +
                    		" It's loaded by the generator class, so if you use a relative filename it's relative to" +
                    		" the generator class, otherwise you might want to load it via an absolute classpath reference like" +
                    		" classpath:/somefile.xml" );
                }
                final File file = new File( resource.toURI() );
                method.invoke( generator, file );
            }
            else {
                method.invoke( generator, new File( filename ) );
            }
        }
        else {
            /* does the param class provide a constructor for string?
             */
            final Constructor<?> paramTypeConstructor = paramClazz.getConstructor( propertyValue.getClass() );
            if ( paramTypeConstructor != null ) {
                final Object typedPropertyValue = paramTypeConstructor.newInstance( propertyValue );
                method.invoke( generator, typedPropertyValue );
            }
            else {
                throw new RuntimeException( "The property '" + propertyName + "' could not be set" +
                        " because the expected parameter is neither of type " + propertyValue.getClass() +
                        " nor of any type that provides a constructor expecting a " + propertyValue.getClass() + "." +
                        " The expected parameter is of type " + paramClazz.getName() );
            }
        }
    }

    private static Method getMethodByName( final String methodName, final Class<?> clazz ) {
        for ( Method method : clazz.getMethods() ) {
            if ( method.getName().equals( methodName ) ) {
                return method;
            }
        }
        throw new RuntimeException( "Method '" + methodName + "' not found for class " + clazz.getName() );
    }

}
