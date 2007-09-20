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

package com.sun.ws.rest.samples.atomserver.resources;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@UriTemplate("/service")
@ProduceMime("application/atomserv+xml")
public class ServiceResource {
    
    @HttpMethod
    public byte[] getService() {

        InputStream in = this.getClass().getResourceAsStream("service.xml");
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build(in);
            Element root = d.getRootElement();
            Namespace ns = root.getNamespace();            
            Element collection = root.getChild("workspace", ns).getChild("collection", ns);
            collection.setAttribute("href", getCollectionUri());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputter xmlo = new XMLOutputter();
            xmlo.output(d, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
    @HttpContext UriInfo uriInfo;
    
    private String getCollectionUri() {
        return uriInfo.getBaseBuilder().path(FeedResource.class).
                build().toString();
    }
}
