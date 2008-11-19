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
package com.sun.jersey.server.impl.wadl;

import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.jersey.server.wadl.WadlGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.research.ws.wadl.Application;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Produces({"application/vnd.sun.wadl+xml", "application/xml"})
public final class WadlResource {
    
    private static final Logger LOGGER = Logger.getLogger( WadlResource.class.getName() );

    private Application _a;
    private String _requiredJaxbContextPath;
    private byte[] _bytes;
    
    public WadlResource(Set<AbstractResource> rootResources,
            WadlGenerator wadlGenerator) {
        _a = new WadlBuilder( wadlGenerator ).generate(rootResources);
        _requiredJaxbContextPath = wadlGenerator.getRequiredJaxbContextPath();
    }
    
    public synchronized @GET Response getWadl(@Context UriInfo uriInfo) {
        
        if ( _bytes == null ) {
            if ( _a.getResources().getBase() == null ) {
                _a.getResources().setBase( uriInfo.getBaseUri().toString() );
            }
            try {
                final JAXBContext jaxbContext = JAXBContext.newInstance(_requiredJaxbContextPath);
                final Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                marshaller.marshal( _a, os );
                _bytes = os.toByteArray();
                os.close();
            } catch ( Exception e ) {
                LOGGER.log( Level.WARNING, "Could not marshal wadl Application.", e );
                return Response.ok( _a ).build();
            }
        }
        
        return Response.ok( new ByteArrayInputStream( _bytes ) ).build();
    }
}
