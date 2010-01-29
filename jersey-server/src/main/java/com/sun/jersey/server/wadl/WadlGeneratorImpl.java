/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.server.wadl;

import com.sun.jersey.api.model.AbstractMethod;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.ParamStyle;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * This WadlGenerator creates the basic wadl artifacts.<br>
 * Created on: Jun 16, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorImpl implements WadlGenerator {

    public String getRequiredJaxbContextPath() {
        final String name = Application.class.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    public void init() throws Exception {
    }

    public void setWadlGeneratorDelegate( WadlGenerator delegate ) {
        throw new UnsupportedOperationException( "No delegate supported." );
    }

    public Resources createResources() {
        return new Resources();
    }

    public Application createApplication() {
        return new Application();
    }

    public com.sun.research.ws.wadl.Method createMethod(
            AbstractResource r, final AbstractResourceMethod m ) {
        com.sun.research.ws.wadl.Method wadlMethod = 
                new com.sun.research.ws.wadl.Method();
        wadlMethod.setName(m.getHttpMethod());
        wadlMethod.setId( m.getMethod().getName() );
        return wadlMethod;
    }

    public RepresentationType createRequestRepresentation( AbstractResource r, AbstractResourceMethod m, MediaType mediaType ) {
        RepresentationType wadlRepresentation = new RepresentationType();
        wadlRepresentation.setMediaType(mediaType.toString());
        return wadlRepresentation;
    }

    public Request createRequest(AbstractResource r, AbstractResourceMethod m) {
        return new Request();
    }

    public Param createParam( AbstractResource r, AbstractMethod m, final Parameter p ) {
        if (p.getSource() == Parameter.Source.UNKNOWN)
            return null;
        Param wadlParam = new Param();
        wadlParam.setName(p.getSourceName());

        /* the form param right now has no Parameter.Source representation
         * and requires some special handling
         */
        if ( p.getAnnotation().annotationType() == FormParam.class ) {
            wadlParam.setStyle( ParamStyle.QUERY );
        }
        else {
            switch (p.getSource()) {
                case QUERY: 
                    wadlParam.setStyle(ParamStyle.QUERY);
                    break;
                case MATRIX:
                    wadlParam.setStyle(ParamStyle.MATRIX);
                    break;
                case PATH:
                    wadlParam.setStyle(ParamStyle.TEMPLATE);
                    break;
                case HEADER:
                    wadlParam.setStyle(ParamStyle.HEADER);
                    break;
                default:
                    break;
            }
        }
        
        if (p.hasDefaultValue())
            wadlParam.setDefault(p.getDefaultValue());
        Class<?> pClass = p.getParameterClass();
        if (pClass.isArray()) {
            wadlParam.setRepeating(true);
            pClass = pClass.getComponentType();
        }
        if (pClass.equals(int.class) || pClass.equals(Integer.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "int", "xs"));
        else if (pClass.equals(boolean.class) || pClass.equals(Boolean.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "boolean", "xs"));
        else if (pClass.equals(long.class) || pClass.equals(Long.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "long", "xs"));
        else if (pClass.equals(short.class) || pClass.equals(Short.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "short", "xs"));
        else if (pClass.equals(byte.class) || pClass.equals(Byte.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "byte", "xs"));
        else if (pClass.equals(float.class) || pClass.equals(Float.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "float", "xs"));
        else if (pClass.equals(double.class) || pClass.equals(Double.class))
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "double", "xs"));
        else
            wadlParam.setType(new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        return wadlParam;
    }

    public Resource createResource( AbstractResource r, String path ) {
        Resource wadlResource = new Resource();
        if (path != null)
            wadlResource.setPath(path);
        else if (r.isRootResource())
            wadlResource.setPath(r.getPath().getValue());
        return wadlResource;
    }

    public Response createResponse( AbstractResource r, AbstractResourceMethod m ) {
        final Response response = new Response();

        for (MediaType mediaType: m.getSupportedOutputTypes()) {
            RepresentationType wadlRepresentation = createResponseRepresentation( r, m, mediaType );
            JAXBElement<RepresentationType> element = new JAXBElement<RepresentationType>(
                    new QName("http://research.sun.com/wadl/2006/10","representation"),
                    RepresentationType.class,
                    wadlRepresentation);
            response.getRepresentationOrFault().add(element);
        }
        
        return response;
    }

    public RepresentationType createResponseRepresentation( AbstractResource r, AbstractResourceMethod m, MediaType mediaType ) {
        RepresentationType wadlRepresentation = new RepresentationType();
        wadlRepresentation.setMediaType(mediaType.toString());
        return wadlRepresentation;
    }
    
}
