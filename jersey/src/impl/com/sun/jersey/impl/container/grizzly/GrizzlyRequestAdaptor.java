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

package com.sun.jersey.impl.container.grizzly;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.jersey.spi.container.AbstractContainerRequest;
import com.sun.jersey.impl.http.header.HttpHeaderFactory;
import com.sun.jersey.spi.container.WebApplication;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public final class GrizzlyRequestAdaptor  extends AbstractContainerRequest {
    
    private final GrizzlyRequest request;
    
    public GrizzlyRequestAdaptor(WebApplication wa, GrizzlyRequest request) 
            throws IOException {
        super(wa, request.getMethod(), request.getInputStream());
        this.request = request;
        
        initiateUriInfo();
        copyHttpHeaders();
    }

    private void initiateUriInfo() {
        try {
            this.baseUri = new URI(
                    request.getScheme(),
                    null,
                    request.getServerName(), 
                    request.getServerPort(),
                    "/", 
                    null, 
                    null);

            /*
             * request.unparsedURI() is a URI in encoded form that contains
             * the URI path and URI query components.
             */
            this.completeUri = baseUri.resolve(
                    request.getRequest().unparsedURI().toString());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex);
        }
    }
    
    private void copyHttpHeaders() {
        MultivaluedMap<String, String> headers = getRequestHeaders();
        
        Enumeration names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            String value = request.getHeader(name);
            headers.add(name, value);
            if (name.equalsIgnoreCase("cookie")) {
                getCookies().putAll(HttpHeaderFactory.createCookies(value));
            }
        }
    }
}
