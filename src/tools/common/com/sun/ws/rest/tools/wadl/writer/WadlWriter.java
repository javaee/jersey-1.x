/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.tools.wadl.writer;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.ParamStyle;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

/**
 * Write a WADL description of the resources found by the apt processor
 * 
 */
public class WadlWriter {
    
    protected Collection<String> resources;
    protected AnnotationProcessorContext context;
    protected String urlPattern;
        
    public WadlWriter(AnnotationProcessorContext context) {
        this(context, null);
    }
    
    public WadlWriter(AnnotationProcessorContext context, String urlPattern) {
        this.context = context;
        this.urlPattern = urlPattern==null ? "" : urlPattern.substring(0,urlPattern.lastIndexOf('/'));
    }
    
    public void writeTo(OutputStream out) throws IOException  {
        Application a = new Application();
        Resources rs = new Resources();
        rs.setBase(com.sun.ws.rest.impl.wadl.WadlReader.BASE_URI_POSITION_MARKER+urlPattern);
        a.setResources(rs);
        for (com.sun.ws.rest.tools.annotation.Resource resourceModel: context.getResources()) {
            Resource r = new Resource();
            r.setPath(resourceModel.getTemplate());
            a.getResources().getResource().add(r);
            for (com.sun.ws.rest.tools.annotation.Method methodModel: resourceModel.getMethods()) {
                com.sun.research.ws.wadl.Method m = new com.sun.research.ws.wadl.Method();
                m.setName(methodModel.getMethodName());
                if (methodModel.getParams().size() > 0) {
                    Request request = new Request();
                    for (com.sun.ws.rest.tools.annotation.Param paramModel: methodModel.getParams()) {
                        if (paramModel.getStyle() == com.sun.ws.rest.tools.annotation.Param.Style.ENTITY)
                            continue;
                        com.sun.research.ws.wadl.Param param = new com.sun.research.ws.wadl.Param();
                        param.setName(paramModel.getName());
                        if (paramModel.getStyle() == com.sun.ws.rest.tools.annotation.Param.Style.QUERY)
                            param.setStyle(ParamStyle.QUERY);
                        else if (paramModel.getStyle() == com.sun.ws.rest.tools.annotation.Param.Style.URI)
                            param.setStyle(ParamStyle.TEMPLATE);
                        if (paramModel.getDefaultValue() != null)
                            param.setDefault(paramModel.getDefaultValue());
                        param.setRepeating(paramModel.isRepeating());
                        if (paramModel.getType() != null)
                            param.setType(paramModel.getType());
                        request.getParam().add(param);
                    }   
                    if (methodModel.isInputEntity()) {
                        RepresentationType rep = new RepresentationType();
                        rep.setMediaType(methodModel.getConsumes());
                        request.getRepresentation().add(rep);
                    }
                    m.setRequest(request);
                }
                if (methodModel.isOutputEntity()) {
                    Response response = new Response();
                    RepresentationType rep = new RepresentationType();
                    rep.setMediaType(methodModel.getProduces());
                    QName elementName = new QName(
                            "http://research.sun.com/wadl/2006/10",
                            "representation");
                    JAXBElement<RepresentationType> repElement =
                            new JAXBElement<RepresentationType>(elementName,
                            RepresentationType.class, rep);
                    response.getRepresentationOrFault().add(repElement);
                    m.setResponse(response);
                }
                r.getMethodOrResource().add(m);
            }
        }
        writeApplication(a, out);
    }

    public void write(PrintWriter writer) throws IOException  {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        writer.print(out.toString());
    }
    
    protected void writeApplication(Application a, OutputStream o) {
        try {
            JAXBContext jbc = JAXBContext.newInstance( "com.sun.research.ws.wadl", 
                this.getClass().getClassLoader() );
            Marshaller m = jbc.createMarshaller();
            m.setProperty("jaxb.formatted.output", true);
            m.marshal(a, o);
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }
}
