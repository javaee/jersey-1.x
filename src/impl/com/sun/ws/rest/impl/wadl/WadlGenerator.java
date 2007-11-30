/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * WadlGenerator.java
 *
 * Created on November 30, 2007, 2:09 PM
 *
 */

package com.sun.ws.rest.impl.wadl;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.ParamStyle;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.api.model.Parameter;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 *
 * @author mh124079
 */
public class WadlGenerator {
    
    public static Application generate(Set<AbstractResource> resources) {
        Application wadlApplication = new Application();
        Resources wadlResources = new Resources();
        
        for (AbstractResource r: resources) {
            Resource wadlResource = new Resource();
            wadlResource.setPath(r.getUriTemplate().getRawTemplate());
            for (AbstractResourceMethod m: r.getResourceMethods()) {
                com.sun.research.ws.wadl.Method wadlMethod = 
                        new com.sun.research.ws.wadl.Method();
                wadlMethod.setName(m.getHttpMethod());
                Request wadlRequest = generateRequest(m);
                if (wadlRequest != null)
                    wadlMethod.setRequest(wadlRequest);
                Response wadlResponse = generateResponse(m);
                if (wadlResponse != null)
                    wadlMethod.setResponse(wadlResponse);
                wadlResource.getMethodOrResource().add(wadlMethod);
            }
            wadlResources.getResource().add(wadlResource);
        }
        wadlApplication.setResources(wadlResources);
        return wadlApplication;
    }

    private static Request generateRequest(final AbstractResourceMethod m) {
        if (m.getParameters().size()==0)
            return null;
        
        Request wadlRequest = new Request();

        for (Parameter p: m.getParameters()) {
            if (p.getSource()==Parameter.Source.ENTITY) {
                for (MediaType mediaType: m.getSupportedInputTypes()) {
                    RepresentationType wadlRepresentation = new RepresentationType();
                    wadlRepresentation.setMediaType(mediaType.toString());
                    wadlRequest.getRepresentation().add(wadlRepresentation);
                }
            } else {
                Param wadlParam = generateParam(p, wadlRequest, m);
                if (wadlParam != null)
                    wadlRequest.getParam().add(wadlParam);
            }
        }
        return wadlRequest;
    }

    private static Param generateParam(final Parameter p, final Request wadlRequest, 
            final AbstractResourceMethod m) {
        if (p.getSource()==Parameter.Source.ENTITY || p.getSource()==Parameter.Source.CONTEXT)
            return null;
        Param wadlParam = new Param();
        wadlParam.setName(p.getSourceName());
        switch (p.getSource()) {
            case QUERY: 
                wadlParam.setStyle(ParamStyle.QUERY);
                break;
            case MATRIX:
                wadlParam.setStyle(ParamStyle.MATRIX);
                break;
            case URI:
                wadlParam.setStyle(ParamStyle.TEMPLATE);
                break;
            case HEADER:
                wadlParam.setStyle(ParamStyle.HEADER);
                break;
            default:
                break;
        }
        return wadlParam;
    }

    private static Response generateResponse(final AbstractResourceMethod m) {
        if (m.getMethod().getReturnType() == void.class)
            return null;
        Response wadlResponse = new Response();
        for (MediaType mediaType: m.getSupportedOutputTypes()) {
            RepresentationType wadlRepresentation = new RepresentationType();
            wadlRepresentation.setMediaType(mediaType.toString());
            JAXBElement<RepresentationType> element = new JAXBElement<RepresentationType>(
                    new QName("http://research.sun.com/wadl/2006/10","representation"),
                    RepresentationType.class,
                    wadlRepresentation);
            wadlResponse.getRepresentationOrFault().add(element);
        }
        return wadlResponse;
    }
    
    
}
