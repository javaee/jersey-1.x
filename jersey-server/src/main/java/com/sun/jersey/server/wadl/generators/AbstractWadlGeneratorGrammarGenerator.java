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
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.ApplicationDescription.ExternalGrammar;
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
import javax.xml.bind.annotation.XmlSeeAlso;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * This {@link WadlGenerator} generates a grammar based on the referenced 
 * elements. This is a template class designed to be overridden by specific
 * implementations, for example XML Schema and JSON Schema
 * </p>
 * Created on: Sept 17, 2012<br>
 *
 * @author Gerard Davison
 * @version $Id: AbstractWadlGeneratorGrammarGenerator.java $
 */
public abstract class AbstractWadlGeneratorGrammarGenerator<T> implements WadlGenerator {

    
    // Wrapper interfaces so I can treat dispirate types the same
    // when processing them later
    //

    protected static interface HasType
    {
        public Class getPrimaryClass();
        public Type getType();
        public MediaType getMediaType();
    }

    protected static interface WantsName<T>
    {
        /**
         * @return does this object need an element or a type
         */
        public boolean isElement();
        public void setName( T name );
    }

    /**
     * @param param parameter.
     * @return An adapter for Parameter
     */
    protected static HasType parameter(final Parameter param, final MediaType mt)
    {
        return new HasType() {
            public Class getPrimaryClass() {
                return param.getParameterClass();
            }
            public Type getType() {
                return param.getParameterType();
            }
            
            public MediaType getMediaType() {
                return mt;
            }
        };
    }



    protected class Pair {

        public Pair(HasType hasType, WantsName wantsName) {
            this.hasType = hasType;
            this.wantsName = wantsName;
        }

        public HasType hasType;
        public WantsName wantsName;
    }

    // Static final fields

    private static final Logger LOGGER = Logger.getLogger( AbstractWadlGeneratorGrammarGenerator.class.getName() );

    public static final java.util.Set<Class> SPECIAL_GENERIC_TYPES =
            new HashSet<Class>()
            {{
                add(JResponse.class);
                add(List.class);
            }};


    // Instance fields

    // The generator we are decorating
    private WadlGenerator _delegate;

    // Any SeeAlso references
    protected Set<Class> _seeAlso;

    // A matched list of Parm, Parameter to list the relavent
    // entity objects that we might like to transform.
    protected List<Pair> _hasTypeWantsName;
    
    // The root of this application
    protected URI _root;
    // The uri of this WADL
    protected URI _wadl;
    
    // Access to the providers
    protected Providers _providers;
    
    protected FeaturesAndProperties _fap;
    
    // The type of the resolved element
    protected Class<T> _resolvedType;

    protected AbstractWadlGeneratorGrammarGenerator(WadlGenerator delegate, Class<T> resolvedType) {
        _delegate = delegate;
        _resolvedType = resolvedType;
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

    
    
    /**
     * Provides the WadlGenerator with the current generating environment.
     * This method is used in a decorator like manner, and therefore has to 
     * invoke <code>this.delegate.setEnvironment(env)</code>.
     */
    @Override
    public void setEnvironment(Environment env)
    {
        _delegate.setEnvironment(env);
        _providers = env.getProviders();
        _fap = env.getFeaturesAndProperties();
    }
    
    // ================ filter actions =======================
    
    /**
     * @param type
     * @return Whether we want to acceptMediaType this element, for example
     *   the grammar generator wouldn't necessarily want to selector
     *   for JAX-B or vice versa.
     */
    public abstract boolean acceptMediaType(MediaType type);

    
    
    
    // =============== Application Creation ================================


    /**
     * @return application
     * @see com.sun.jersey.server.wadl.WadlGenerator#createApplication()
     */
    @Override
    public Application createApplication(UriInfo requestInfo) {
        if (requestInfo!=null) {
            _root = requestInfo.getBaseUri();
            _wadl = requestInfo.getRequestUri();
        }
        
        return _delegate.createApplication(requestInfo);
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
                    parameter(p, MediaType.APPLICATION_XML_TYPE), createParmWantsName(param)));
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
                if (acceptMediaType(mt)) {
                    _hasTypeWantsName.add( new Pair(
                            parameter(p, mt), createRepresentationWantsName(rt)));
                }
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

            for(Response response : responses) {
                for (final Representation representation : response.getRepresentation()) {

                    // Process each representation
                    // Check first that we have a media type and that it is supported
                    if (representation.getMediaType()!=null && acceptMediaType(MediaType.valueOf(representation.getMediaType()))) {

                        
                        HasType hasType = new HasType()  {
                            @Override
                            public Class getPrimaryClass() {
                                return arm.getReturnType();
                            }

                            public Type getType() {
                                return arm.getGenericReturnType();
                            }
                            
                            public MediaType getMediaType() {
                                return MediaType.valueOf(representation.getMediaType());
                            }
                        };
                        
                        _hasTypeWantsName.add(new Pair(
                                hasType,
                                createRepresentationWantsName(representation)));
                    }
                }
            }
        }
        return responses;
    }

    // ================ methods for post build actions =======================

    public ExternalGrammarDefinition createExternalGrammar() {

        // Invoke previous generator so we can overide if required
        //
        
        ExternalGrammarDefinition previous = _delegate.createExternalGrammar();
        
        
        // Right now lets generate some external metadata

        Map<String, ApplicationDescription.ExternalGrammar> extraFiles =
                new HashMap<String, ApplicationDescription.ExternalGrammar>();

        // Build the model as required
        Resolver resolver = buildModelAndSchemas(extraFiles);

        // Pass onto the next delegate
        previous.map.putAll(extraFiles);
        if (resolver!=null) {
            previous.addResolver(resolver);
        }

        return previous;
    }

    /**
     * Build the the external schema files and generate a suitable resolver
     * @param extraFiles
     */
    protected abstract Resolver buildModelAndSchemas(Map<String, ExternalGrammar> extraFiles);


    @Override
    public void attachTypes(ApplicationDescription introspector) {

        // Chain
        _delegate.attachTypes(introspector);
        
        // If we managed to get an introspector then lets go back an update the parameters

        if (introspector!=null) {

            int i = _hasTypeWantsName.size();
            nextItem : for ( int j = 0; j < i; j++ ) {

                Pair pair = _hasTypeWantsName.get( j );
                WantsName nextToProcess = pair.wantsName;
                
                // If we have to resolve a type rather than an element
                // then we can't do this yet
                //
                
                if (!nextToProcess.isElement()) {
                    // This is only a problem for param types; but these are 
                    // nearly always basic a scalar types, out of scope of
                    // the current modifications to resolve this issue
                    LOGGER.info("Type references are not supported as yet");
                }
                
                //
                
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
                        LOGGER.info("Couldn't find grammar element due to nested parameterized type " + type);
                        return;
                    }
                }

                T name = introspector.resolve(parameterClass, nextType.getMediaType(), _resolvedType);

                if ( name !=null ) {
                    nextToProcess.setName(name);
                } else  {
                    LOGGER.info("Couldn't find grammar element for class " + parameterClass.getName());
                }
            }
        }
    }
    
    // ================ methods for creating wants name actions ===============
    
    protected abstract WantsName<T> createParmWantsName(final Param param);

    protected abstract WantsName<T> createRepresentationWantsName(final Representation rt);
 
    
}
