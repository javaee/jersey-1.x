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
package com.sun.jersey.impl.wadl.generators;

import com.sun.jersey.api.model.AbstractMethod;
import java.io.File;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * This {@link WadlGenerator} adds the provided {@link Grammars} element to the
 * generated wadl-file.<br>
 * Created on: Jun 24, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorGrammarsSupport implements WadlGenerator {
    
    private static final Logger LOG = Logger.getLogger( WadlGeneratorGrammarsSupport.class.getName() );

    private WadlGenerator _delegate;
    private File _grammarsFile;
    private Grammars _grammars;
    
    public WadlGeneratorGrammarsSupport() {
    }

    public WadlGeneratorGrammarsSupport( WadlGenerator delegate,
            Grammars grammars ) {
        _delegate = delegate;
        _grammars = grammars;
    }
    
    public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        _delegate = delegate;
    }

    public String getRequiredJaxbContextPath() {
        return _delegate.getRequiredJaxbContextPath();
    }
    
    public void setGrammarsFile( File grammarsFile ) {
        _grammarsFile = grammarsFile;
        LOG.info( "Setting grammarsFile " + grammarsFile.getAbsolutePath() );
    }
    
    public void init() throws Exception {
        _delegate.init();
        _grammars = loadFile( _grammarsFile, Grammars.class );
    }
    
    private <T> T loadFile( File fileToLoad, Class<T> targetClass ) throws JAXBException {
        final JAXBContext c = JAXBContext.newInstance( targetClass );
        final Unmarshaller m = c.createUnmarshaller();
        return targetClass.cast( m.unmarshal( fileToLoad ) );
    }

    /**
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createApplication()
     */
    public Application createApplication() {
        final Application result = _delegate.createApplication();
        if ( result.getGrammars() != null ) {
            LOG.info( "The wadl application created by the delegate ("+ _delegate +") already contains a grammars element," +
            		" we're adding elements of the provided grammars file." );
            if ( !_grammars.getAny().isEmpty() ) {
                result.getGrammars().getAny().addAll( _grammars.getAny() );
            }
            if ( !_grammars.getDoc().isEmpty() ) {
                result.getGrammars().getDoc().addAll( _grammars.getDoc() );
            }
            if ( !_grammars.getInclude().isEmpty() ) {
                result.getGrammars().getInclude().addAll( _grammars.getInclude() );
            }
        }
        else {
            result.setGrammars( _grammars );
        }
        return result;
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createMethod(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Method createMethod( AbstractResource arg0,
            AbstractResourceMethod arg1 ) {
        return _delegate.createMethod( arg0, arg1 );
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createRequest(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Request createRequest( AbstractResource arg0,
            AbstractResourceMethod arg1 ) {
        return _delegate.createRequest( arg0, arg1 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createParam(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractMethod, com.sun.jersey.api.model.Parameter)
     */
    public Param createParam( AbstractResource arg0,
            AbstractMethod arg1, Parameter arg2 ) {
        return _delegate.createParam( arg0, arg1, arg2 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createRequestRepresentation(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod, javax.ws.rs.core.MediaType)
     */
    public RepresentationType createRequestRepresentation(
            AbstractResource arg0, AbstractResourceMethod arg1, MediaType arg2 ) {
        return _delegate.createRequestRepresentation( arg0, arg1, arg2 );
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createResource(com.sun.jersey.api.model.AbstractResource, java.lang.String)
     */
    public Resource createResource( AbstractResource arg0, String arg1 ) {
        return _delegate.createResource( arg0, arg1 );
    }

    /**
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createResources()
     */
    public Resources createResources() {
        return _delegate.createResources();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see com.sun.jersey.impl.wadl.WadlGenerator#createResponse(com.sun.jersey.api.model.AbstractResource, com.sun.jersey.api.model.AbstractResourceMethod)
     */
    public Response createResponse( AbstractResource arg0,
            AbstractResourceMethod arg1 ) {
        return _delegate.createResponse( arg0, arg1 );
    }

}
