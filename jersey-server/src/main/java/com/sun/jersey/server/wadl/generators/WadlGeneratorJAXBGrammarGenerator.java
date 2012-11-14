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
package com.sun.jersey.server.wadl.generators;

import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.ApplicationDescription.ExternalGrammar;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link WadlGenerator} generates a XML Schema content model based on
 * referenced java beans.
 * </p>
 * Created on: Jun 22, 2011<br>
 *
 * @author Gerard Davison
 * @version $Id: WadlGeneratorJAXBGrammarGenerator.java $
 */
public class WadlGeneratorJAXBGrammarGenerator extends AbstractWadlGeneratorGrammarGenerator<QName> {


    // Static final fields

    private static final Logger LOGGER = Logger.getLogger( WadlGeneratorJAXBGrammarGenerator.class.getName() );


    // Instance fields


    public WadlGeneratorJAXBGrammarGenerator() {
        super(new WadlGeneratorImpl(), QName.class);
    }

    // ================ filter actions =======================
    
    /**
     * @return true if the media type appears to be a XML type
     */
    public boolean acceptMediaType(MediaType type) {
        if (type.equals(MediaType.APPLICATION_XML_TYPE)
            || type.equals(MediaType.TEXT_XML_TYPE)
            || type.getSubtype().endsWith("+xml") ) {
            return true;
        }
        // For client that support XSL schema -> JSON mapping
        else if (type.equals(MediaType.APPLICATION_JSON_TYPE)
            || type.getSubtype().endsWith("+json") ) {
            return true;
        }
        else if (type.equals(MediaType.WILDCARD_TYPE)) {
            // For backward compatibility match the wildcard type
            //
            return true;
        }
        else
        {
            return false;
        }
    }

    
    // ================ methods for post build actions =======================


    /**
     * Build the JAXB model and generate the schemas based on tha data
     * @param extraFiles
     */
    @Override
    protected Resolver buildModelAndSchemas(Map<String, ExternalGrammar> extraFiles) {

        // Lets get all candidate classes so we can create the JAX-B context
        // include any @XmlSeeAlso references.

        Set<Class> classSet = new HashSet<Class>(_seeAlso);

        for ( Pair pair : _hasTypeWantsName ) {
            HasType hasType = pair.hasType;
            Class clazz = hasType.getPrimaryClass();

            // Is this class itself interesting?

            if ( clazz.getAnnotation( XmlRootElement.class ) !=null ) {
                classSet.add( clazz );
            }
            else if ( SPECIAL_GENERIC_TYPES.contains (clazz) ) {
 
                Type type = hasType.getType();
                if ( type instanceof ParameterizedType )
                {
                    Type parameterType = ((ParameterizedType)type).getActualTypeArguments()[0];
                    if (parameterType instanceof Class)
                    {
                        classSet.add( (Class) parameterType );
                    }
                }
            }
        }

        // Create a JAX-B context, and use this to generate us a bunch of
        // schema objects

        JAXBIntrospector introspector = null;

        try {
            JAXBContext context = JAXBContext.newInstance( classSet.toArray( new Class[classSet.size()] ) );

            final List<StreamResult> results = new ArrayList<StreamResult>();

            context.generateSchema( new SchemaOutputResolver() {

                int counter = 0;

                @Override
                public Result createOutput( String namespaceUri, String suggestedFileName ) {
                    StreamResult result = new StreamResult( new CharArrayWriter() );
                    result.setSystemId( "xsd" + (counter++)  + ".xsd");
                    results.add(result);
                    return result;
                }
            });

            // Store the new files for later use
            //

            for (StreamResult result : results) {
                CharArrayWriter writer = ( CharArrayWriter )result.getWriter();
                byte[] contents = writer.toString().getBytes( "UTF8" );
                extraFiles.put(
                        result.getSystemId() ,
                        new ApplicationDescription.ExternalGrammar(
                                MediaType.APPLICATION_XML_TYPE, // I don't think there is a specific media type for XML Schema
                                contents,
                                true));
            }

            // Create an introspector
            //

            introspector = context.createJAXBIntrospector();


        } catch ( JAXBException e ) {
            LOGGER.log( Level.SEVERE, "Failed to generate the schema for the JAX-B elements", e );
        }
        catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, "Failed to generate the schema for the JAX-B elements due to an IO error", e );
        }

        // Create introspector

        if (introspector!=null) {
            final JAXBIntrospector copy = introspector;

            return new Resolver() {
                @Override
                public <T> T resolve(Class type, MediaType mt, Class<T> resolvedType) {
                    
                    // We only return a QName
                    if (!QName.class.equals(resolvedType)) {
                        return null;
                    }
                    
                    if (!acceptMediaType(mt)) {
                        return null;
                    }

                    Object parameterClassInstance = null;
                    try {
                        Constructor<?> defaultConstructor = type.getDeclaredConstructor();
                        defaultConstructor.setAccessible(true);
                        parameterClassInstance = defaultConstructor.newInstance();
                    } catch (InstantiationException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (IllegalAccessException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (InvocationTargetException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (SecurityException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (NoSuchMethodException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }

                    if (parameterClassInstance==null) {
                        return null;
                    }

                    try {
                        return resolvedType.cast(
                                copy.getElementName(parameterClassInstance));
                    } catch (NullPointerException e) {
                        // EclipseLink throws an NPE if an object annotated with @XmlType and without the @XmlRootElement
                        // annotation is passed as a parameter of #getElementName method.
                        return null;
                    }
                }

            };
        }
        else {
            return null; // No resolver created
        }
    }

    
    
    // ================ methods for creating wants name actions ===============
    
    @Override
    protected WantsName<QName> createParmWantsName(final Param param) {
        return new WantsName<QName>() {
                   @Override
                   public boolean isElement()
                   {
                       return false;
                   }
                   
                   @Override
                   public void setName(QName name) {
                       // TODO this is a type reference, not a element
                       // reference so we actually have to find the original
                       // type in this case
                       param.setType(name);
                   }
               };
    }

    @Override
    protected WantsName<QName> createRepresentationWantsName(final Representation rt) {
        return new WantsName<QName>() {
                   @Override
                   public boolean isElement()
                   {
                       return true;
                   }

                   @Override
                   public void setName(QName name) {
                       rt.setElement(name);
                   }
               };
    }
    
}
