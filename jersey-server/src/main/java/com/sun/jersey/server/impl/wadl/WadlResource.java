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

package com.sun.jersey.server.impl.wadl;

import com.sun.jersey.server.wadl.WadlApplicationContext;
import com.sun.jersey.spi.resource.Singleton;
import com.sun.research.ws.wadl.Application;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.Marshaller;


/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Produces({"application/vnd.sun.wadl+xml", "application/xml"})
@Singleton
public final class WadlResource {

    private static final Logger LOGGER = Logger.getLogger(WadlResource.class.getName());

    private WadlApplicationContext wadlContext;

    private Application application;

    private byte[] wadlXmlRepresentation;

    public WadlResource(@Context WadlApplicationContext wadlContext) {
        this.wadlContext = wadlContext;
        this.application = wadlContext.getApplication();
    }

    @GET
    public synchronized Response getWadl(@Context UriInfo uriInfo) {
        if (wadlXmlRepresentation == null) {
            if (application.getResources().getBase() == null) {
                application.getResources().setBase(uriInfo.getBaseUri().toString());
            }
            try {
                final Marshaller marshaller = wadlContext.getJAXBContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                marshaller.marshal(application, os);
                wadlXmlRepresentation = os.toByteArray();
                os.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not marshal wadl Application.", e);
                return Response.ok(application).build();
            }
        }

        return Response.ok(new ByteArrayInputStream(wadlXmlRepresentation)).build();
    }
}