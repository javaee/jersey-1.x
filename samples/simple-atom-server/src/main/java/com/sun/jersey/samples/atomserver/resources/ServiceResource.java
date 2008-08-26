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

package com.sun.jersey.samples.atomserver.resources;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
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
@Path("/service")
@Produces("application/atomserv+xml")
public class ServiceResource {
    
    @GET
    public byte[] getService() {

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("service.xml");
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
    
    @Context UriInfo uriInfo;
    
    private String getCollectionUri() {
        return uriInfo.getBaseUriBuilder().path(FeedResource.class).
                build().toString();
    }
}
