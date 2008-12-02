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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Path("jaxb/array")
@Produces("application/xml")
@Consumes("application/xml")
public class JAXBArrayResource {

    @Path("XmlRootElement")
    @GET
    public JAXBXmlRootElement[] getRootElement() {
        List<JAXBXmlRootElement> el = new ArrayList<JAXBXmlRootElement>();
        el.add(new JAXBXmlRootElement("one root element"));
        el.add(new JAXBXmlRootElement("two root element"));
        el.add(new JAXBXmlRootElement("three root element"));
        return el.toArray(new JAXBXmlRootElement[el.size()]);
    }

    @Path("XmlRootElement")
    @POST
    public JAXBXmlRootElement[] postRootElement(JAXBXmlRootElement[] el) {
        return el;
    }
    
    @Path("XmlType")
    @POST
    public JAXBXmlRootElement[] postXmlType(JAXBXmlType[] tl) {
        List<JAXBXmlRootElement> el = new ArrayList<JAXBXmlRootElement>();

        for (JAXBXmlType t : tl) 
            el.add(new JAXBXmlRootElement(t.value));

        return el.toArray(new JAXBXmlRootElement[el.size()]);
    }
}