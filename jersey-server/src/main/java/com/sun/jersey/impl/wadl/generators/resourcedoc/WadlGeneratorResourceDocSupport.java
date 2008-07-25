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
package com.sun.jersey.impl.wadl.generators.resourcedoc;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ParamDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.RepresentationDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ResourceDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ResponseDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.WadlParamType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.xhtml.Elements;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Doc;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.ParamStyle;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Jun 16, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorResourceDocSupport implements WadlGenerator {

    private static final Logger LOG = Logger.getLogger( WadlGeneratorResourceDocSupport.class.getName() );
    public static final String RESOURCE_DOC_FILE = "resourcedoc.xml";

    private WadlGenerator _delegate;
    private File _resourceDocFile;
    private ResourceDocAccessor _resourceDoc;
    
    public WadlGeneratorResourceDocSupport() {
    }

    public WadlGeneratorResourceDocSupport( WadlGenerator wadlGenerator, ResourceDocType resourceDoc ) {
        _delegate = wadlGenerator;
        _resourceDoc = new ResourceDocAccessor( resourceDoc );
    }

    public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        _delegate = delegate;
    }
    
    public void setResourceDocFile( File resourceDocFile ) {
        _resourceDocFile = resourceDocFile;
    }
    
    public void init() throws Exception {
        _delegate.init();
        final ResourceDocType resourceDoc = loadFile( _resourceDocFile, ResourceDocType.class );
        _resourceDoc = new ResourceDocAccessor( resourceDoc );
    }

    private <T> T loadFile( File fileToLoad, Class<T> targetClass ) {
        if ( fileToLoad != null ) {
            try {
                final JAXBContext c = JAXBContext.newInstance( targetClass );
                final Unmarshaller m = c.createUnmarshaller();
                return targetClass.cast( m.unmarshal( fileToLoad ) );
            } catch( Exception e ) {
                LOG.log( Level.SEVERE, "Could not unmarshal file " + fileToLoad, e );
            }
        }
        return null;
    }
    
    public String getRequiredJaxbContextPath() {
        return _delegate.getRequiredJaxbContextPath() == null
            ? Elements.class.getPackage().getName()
            : _delegate.getRequiredJaxbContextPath() +
                ":" + Elements.class.getPackage().getName();
    }

    /**
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createApplication()
     */
    public Application createApplication() {
        return _delegate.createApplication();
    }

    /**
     * @param r
     * @param path
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createResource(com.sun.jersey.api.model.AbstractResource, java.lang.String)
     */
    public Resource createResource( AbstractResource r, String path ) {
        final Resource result = _delegate.createResource( r, path );
        final ClassDocType classDoc = _resourceDoc.getClassDoc( r.getResourceClass() );
        if ( classDoc != null && !isEmpty( classDoc.getCommentText() ) ) {
            final Doc doc = new Doc();
            doc.getContent().add( classDoc.getCommentText() );
            result.getDoc().add( doc );
        }
        return result;
    }

    /**
     * @param r
     * @param m
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createMethod(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Method createMethod( AbstractResource r, AbstractResourceMethod m ) {
        final Method result = _delegate.createMethod( r, m );
        final MethodDocType methodDoc = _resourceDoc.getMethodDoc( r.getResourceClass(), m.getMethod() );
        if ( methodDoc != null && !isEmpty( methodDoc.getCommentText() ) ) {
            final Doc doc = new Doc();
            doc.getContent().add( methodDoc.getCommentText() );
            // doc.getOtherAttributes().put( new QName( "xmlns" ), "http://www.w3.org/1999/xhtml" );
            result.getDoc().add( doc );
        }
        return result;
    }

    /**
     * @param r
     * @param m
     * @param mediaType
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createRequestRepresentation(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod, javax.ws.rs.core.MediaType)
     */
    public RepresentationType createRequestRepresentation( AbstractResource r,
            AbstractResourceMethod m, MediaType mediaType ) {
        final RepresentationType result = _delegate.createRequestRepresentation( r, m, mediaType );
        final RepresentationDocType requestRepresentation = _resourceDoc.getRequestRepresentation( r.getResourceClass(), m.getMethod(), result.getMediaType() );
        if ( requestRepresentation != null ) {
            result.setElement( requestRepresentation.getElement() );
            addDocForExample( result.getDoc(), requestRepresentation.getExample() );
        }
        return result;
    }

    /**
     * @param r
     * @param m
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createRequest(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Request createRequest( AbstractResource r, AbstractResourceMethod m ) {
        return _delegate.createRequest( r, m );
    }

    /**
     * @param r
     * @param m
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createResponse(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Response createResponse( AbstractResource r, AbstractResourceMethod m ) {
        final ResponseDocType responseDoc = _resourceDoc.getResponse( r.getResourceClass(), m.getMethod() );
        final Response response;
        if ( responseDoc != null && responseDoc.hasRepresentations() ) {
            response = new Response();
            
            for ( RepresentationDocType representationDoc : responseDoc.getRepresentations() ) {
                
                final RepresentationType wadlRepresentation = new RepresentationType();
                wadlRepresentation.setElement( representationDoc.getElement() );
                wadlRepresentation.getStatus().add( representationDoc.getStatus() );
                wadlRepresentation.setMediaType( representationDoc.getMediaType() );
                addDocForExample( wadlRepresentation.getDoc(), representationDoc.getExample() );
                addDoc( wadlRepresentation.getDoc(), representationDoc.getDoc() );
                
                JAXBElement<RepresentationType> element = new JAXBElement<RepresentationType>(
                        new QName("http://research.sun.com/wadl/2006/10","representation"),
                        RepresentationType.class,
                        wadlRepresentation);
                
                response.getRepresentationOrFault().add(element);
            }
            
            return response;
        }
        else {
            response = _delegate.createResponse( r, m );
        }
        
        /* add response params from resourcedoc
         */
        if ( responseDoc != null && !responseDoc.getWadlParams().isEmpty() ) {
            for ( WadlParamType wadlParamType : responseDoc.getWadlParams() ) {
                final Param param = new Param();
                param.setName( wadlParamType.getName() );
                param.setStyle( ParamStyle.fromValue( wadlParamType.getStyle() ) );
                param.setType( wadlParamType.getType() );
                addDoc( param.getDoc(), wadlParamType.getDoc() );
                response.getParam().add( param );
            }
        }
        
        if ( responseDoc != null && !isEmpty( responseDoc.getReturnDoc() ) ) {
            addDoc( response.getDoc(), responseDoc.getReturnDoc() );
        }
        
        return response;
    }

    private void addDocForExample( final List<Doc> docs, final String example ) {
        if ( !isEmpty( example ) ) {
            final Doc doc = new Doc();
            
            final Elements pElement = Elements.el( "p" )
                .add( Elements.val( "h6", "Example" ) )
                .add( Elements.el( "pre" ).add( Elements.val( "code", example ) ) );
            
            doc.getContent().add( pElement );
            docs.add( doc );
        }
    }
    
    private void addDoc( final List<Doc> docs, final String text ) {
        if ( !isEmpty( text ) ) {
            final Doc doc = new Doc();
            doc.getContent().add( text );
            docs.add( doc );
        }
    }

    /**
     * @param r
     * @param m
     * @param p
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createRequestParam(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod, com.sun.jersey.api.model.Parameter)
     */
    public Param createRequestParam( AbstractResource r,
            AbstractResourceMethod m, Parameter p ) {
        final Param result = _delegate.createRequestParam( r, m, p );
        final ParamDocType paramDoc = _resourceDoc.getParamDoc( r.getResourceClass(), m.getMethod(), p );
        if ( paramDoc != null && !isEmpty( paramDoc.getCommentText() ) ) {
            final Doc doc = new Doc();
            doc.getContent().add( paramDoc.getCommentText() );
            result.getDoc().add( doc );
        }
        return result;
    }

    /**
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createResources()
     */
    public Resources createResources() {
        return _delegate.createResources();
    }

    private boolean isEmpty( String text ) {
        return text == null || text.length() == 0 || "".equals( text.trim() );
    }

}
