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
package com.sun.jersey.samples.jaxb;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Path("jaxb")
@Produces("application/xml")
@Consumes("application/xml")
public class JAXBResource {

    @Path("XmlRootElement")
    @GET
    public JAXBXmlRootElement getRootElement() {
        return new JAXBXmlRootElement("xml root element");
    }

    @Path("XmlRootElement")
    @POST
    public JAXBXmlRootElement postRootElement(JAXBXmlRootElement r) {
        return r;
    }

    
    @Path("JAXBElement")
    @GET
    public JAXBElement<JAXBXmlType> getJAXBElement() {
        return new JAXBElement<JAXBXmlType>(
                new QName("jaxbXmlRootElement"),
                JAXBXmlType.class,
                new JAXBXmlType("xml type"));
    }

    @Path("JAXBElement")
    @POST
    public JAXBElement<JAXBXmlType> postJAXBElement(JAXBElement<JAXBXmlType> e) {
        return e;
    }

    
    @Path("XmlType")
    @POST
    public JAXBElement<JAXBXmlType> postXmlType(JAXBXmlType r) {
        return new JAXBElement<JAXBXmlType>(
                new QName("jaxbXmlRootElement"), JAXBXmlType.class, r);
    }
}