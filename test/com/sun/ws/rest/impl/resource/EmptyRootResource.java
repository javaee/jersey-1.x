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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyRootResource extends AbstractResourceTester {
    
    public EmptyRootResource(String testName) {
        super(testName);
    }

    @Path("/")
    public static class EmptyResource {
    }
    
    public void testGet() throws IOException {
        initiateWebApplication(EmptyResource.class);

        WebResource r = resource("/", false);
        ClientResponse rib = r.get(ClientResponse.class);
        assertEquals(405, rib.getStatus());
    }
}