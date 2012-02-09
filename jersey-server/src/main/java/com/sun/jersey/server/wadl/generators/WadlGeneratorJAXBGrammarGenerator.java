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

import com.sun.jersey.api.JResponse;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.ApplicationDescription.ExternalGrammar;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
public class WadlGeneratorJAXBGrammarGenerator implements WadlGenerator {

    // Wrapper interfaces so I can treat dispirate types the same
    // when processing them later
    //

    private static interface HasType
    {
        public Class getPrimaryClass();
        public Type getType();
    }

    private static interface WantsName
    {
        public void setName( QName name );
    }

    /**
     * @param param parameter.
     * @return An adapter for Parameter
     */
    private static HasType parameter(final Parameter param)
    {
        return new HasType() {
            public Class getPrimaryClass() {
                return param.getParameterClass();
            }
            public Type getType() {
                return param.getParameterType();
            }
        };
    }


    private class Pair {

        public Pair(HasType hasType, WantsName wantsName) {
            this.hasType = hasType;
            this.wantsName = wantsName;
        }

        HasType hasType;
        WantsName wantsName;
    }

    // Static final fields

    private static final Logger LOGGER = Logger.getLogger( WadlGeneratorJAXBGrammarGenerator.class.getName() );

    private static final java.util.Set<Class> SPECIAL_GENERIC_TYPES =
            new HashSet<Class>()
            {{
                    add(JResponse.class);
                    add(List.class);
                }};


    // Instance fields

    // The generator we are decorating
    private WadlGenerator _delegate;

    // Any SeeAlso references
    private Set<Class> _seeAlso;

    // A matched list of Parm, Parameter to list the relavent
    // entity objects that we might like to transform.
    private List<Pair> _hasTypeWantsName;

    public WadlGeneratorJAXBGrammarGenerator() {
        _delegate = new WadlGeneratorImpl();
    }

    // =============== House keeping methods ================================

    public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        _delegate = delegate;
    }

    public String getRequiredJaxbContextPath() {
        return _delegate.getRequiredJaxbContextPath();
    }



    public void init() throws Exception {
        _delegate.init();
        //
        _seeAlso = new HashSet<Class>();

        // A matched list of Parm, Parameter to list the relavent
        // entity objects that we might like to transform.
        _hasTypeWantsName = new ArrayList<Pair>();
    }

    // =============== Application Creation ================================


    /**
     * @return application
     * @see com.sun.jersey.server.wadl.WadlGenerator#createApplication()
     */
    public Application createApplication() {
        return _delegate.createApplication();
    }

    /**
     * @param ar abstract resource
     * @param arm abstract resource method
     * @return method
     * @see com.sun.jersey.server.wadl.WadlGenerator#createMethod(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Method createMethod( AbstractResource ar,
                                AbstractResourceMethod arm ) {
        return _delegate.createMethod( ar, arm );
    }

    /**
     * @param ar abstract resource
     * @param arm abstract resource method
     * @return request
     * @see com.sun.jersey.server.wadl.WadlGenerator#createRequest(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Request createRequest( AbstractResource ar,
                                  AbstractResourceMethod arm ) {

        return _delegate.createRequest( ar, arm );
    }

    /**
     * @param ar abstract resource
     * @param am abstract method
     * @param p parameter
     * @return parameter
     * @see com.sun.jersey.server.wadl.WadlGenerator#createParam(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractMethod, com.sun.jersey.api.model.Parameter)
     */
    public Param createParam( AbstractResource ar,
                              AbstractMethod am, Parameter p ) {
        final Param param = _delegate.createParam( ar, am, p );

        // If the paramter is an entity we probably want to convert this to XML
        //
        if (p.getSource() == Parameter.Source.ENTITY) {
            _hasTypeWantsName.add( new Pair(
                    parameter(p),
                    new WantsName() {
                        public void setName(QName name) {
                            param.setType(name);
                        }
                    }));
        }

        return param;
    }

    /**
     * @param ar abstract resource
     * @param arm abstract resource method
     * @param mt media type
     * @return respresentation type
     * @see com.sun.jersey.server.wadl.WadlGenerator#createRequestRepresentation(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod, javax.ws.rs.core.MediaType)
     */
    public Representation createRequestRepresentation(
            AbstractResource ar, AbstractResourceMethod arm, MediaType mt ) {

        final Representation rt = _delegate.createRequestRepresentation( ar, arm, mt );

        for (Parameter p : arm.getParameters()) {
            if (p.getSource() == Parameter.Source.ENTITY) {
                _hasTypeWantsName.add( new Pair(
                        parameter(p),
                        new WantsName() {
                            @Override
                            public void setName(QName name) {
                                rt.setElement(name);
                            }
                        }));
            }
        }

        return rt;
    }

    /**
     * @param ar abstract resource
     * @param path resources path
     * @return resource
     * @see com.sun.jersey.server.wadl.WadlGenerator#createResource(com.sun.jersey.api.model.AbstractResource, java.lang.String)
     */
    public Resource createResource( AbstractResource ar, String path ) {

        Class cls = ar.getResourceClass();
        XmlSeeAlso seeAlso = (XmlSeeAlso)cls.getAnnotation( XmlSeeAlso.class );
        if ( seeAlso !=null ) {
            Collections.addAll(_seeAlso, seeAlso.value());
        }

        return _delegate.createResource( ar, path );
    }

    /**
     * @return resources
     * @see com.sun.jersey.server.wadl.WadlGenerator#createResources()
     */
    public Resources createResources() {
        return _delegate.createResources();
    }

    /**
     * @param ar abstract resource
     * @param arm abstract resource method
     * @return response
     * @see com.sun.jersey.server.wadl.WadlGenerator#createResponses(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public List<Response> createResponses(AbstractResource ar,
                                          final AbstractResourceMethod arm) {
        final List<Response> responses = _delegate.createResponses(ar, arm );
        if (responses!=null) {
            HasType hasType = new HasType()  {
                @Override
                public Class getPrimaryClass() {
                    return arm.getReturnType();
                }

                public Type getType() {
                    return arm.getGenericReturnType();
                }
            };

            for(Response response : responses) {
                for (final Representation representation : response.getRepresentation()) {

                    // Process each representation
                    _hasTypeWantsName.add(new Pair(
                            hasType,
                            new WantsName() {
                                @Override
                                public void setName(QName name) {
                                    representation.setElement(name);
                                }
                            }));
                }
            }
        }
        return responses;
    }

    // ================ methods for post build actions =======================

    public ExternalGrammarDefinition createExternalGrammar() {

        // Right now lets generate some external metadata

        Map<String, ApplicationDescription.ExternalGrammar> extraFiles =
                new HashMap<String, ApplicationDescription.ExternalGrammar>();

        // Build the model as required
        Resolver resolver = buildModelAndSchemas(extraFiles);

        // Pass onto the next delegate
        ExternalGrammarDefinition previous = _delegate.createExternalGrammar();
        previous.map.putAll(extraFiles);
        if (resolver!=null) {
            previous.addResolver(resolver);
        }

        return previous;
    }

    /**
     * Build the JAXB model and generate the schemas based on tha data
     * @param extraFiles
     */
    private Resolver buildModelAndSchemas(Map<String, ExternalGrammar> extraFiles) {

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
                                contents));
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
                public QName resolve(Class type) {

                    Object parameterClassInstance = null;
                    try {
                        parameterClassInstance = type.newInstance();
                    } catch (InstantiationException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (IllegalAccessException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }

                    if (parameterClassInstance==null) {
                        return null;
                    }

                    return copy.getElementName(parameterClassInstance);

                }
            };
        }
        else {
            return null; // No resolver created
        }
    }

    @Override
    public void attachTypes(ApplicationDescription introspector) {

        // If we managed to get an introspector then lets go back an update the parameters

        if (introspector!=null) {

            int i = _hasTypeWantsName.size();
            nextItem : for ( int j = 0; j < i; j++ ) {

                Pair pair = _hasTypeWantsName.get( j );
                WantsName nextToProcess = pair.wantsName;
                HasType nextType = pair.hasType;

                // There is a method on the RI version that works with just
                // the class name; but using the introspector for the moment
                // as it leads to cleaner code

                Class<?> parameterClass = nextType.getPrimaryClass();

                // Fix those specific generic types
                if (SPECIAL_GENERIC_TYPES.contains(parameterClass)) {
                    Type type = nextType.getType();

                    if (ParameterizedType.class.isAssignableFrom(type.getClass()) &&
                            Class.class.isAssignableFrom(((ParameterizedType)type).getActualTypeArguments()[0].getClass())) {
                        parameterClass = (Class) ((ParameterizedType)type).getActualTypeArguments()[0];
                    } else {
                        // Works around JERSEY-830
                        LOGGER.info("Couldn't find JAX-B element due to nested parameterized type " + type);
                        return;
                    }
                }

                QName name = introspector.resolve(parameterClass);

                if ( name !=null ) {
                    nextToProcess.setName(name);
                } else  {
                    LOGGER.info("Couldn't find JAX-B element for class " + parameterClass.getName());
                }
            }
        }
    }
}
