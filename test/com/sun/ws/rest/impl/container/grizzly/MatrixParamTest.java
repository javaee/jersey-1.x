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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.UriBuilder;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MatrixParamTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class MatrixParamResource {
        @GET
        public String get(@MatrixParam("x") String x, @MatrixParam("y") String y) {
            return y;
        }
    }
        
    public MatrixParamTest(String testName) {
        super(testName);
    }
    
    public void testMatrixParam() {
        startServer(MatrixParamResource.class);

        UriBuilder base = getUri().path("test");
            
        WebResource r = Client.create().resource(base.clone().matrixParam("y", "1").build());
        assertEquals("1", r.get(String.class));
        r = Client.create().resource(base.clone().
                matrixParam("x", "1").encode(false).matrixParam("y", "1%20%2B%202").build());
        assertEquals("1 + 2", r.get(String.class));
        r = Client.create().resource(base.clone().
                matrixParam("x", "1").encode(false).matrixParam("y", "1%20%26%202").build());
        assertEquals("1 & 2", r.get(String.class));
        r = Client.create().resource(base.clone().
                matrixParam("x", "1").encode(false).matrixParam("y", "1%20%7C%7C%202").build());
        assertEquals("1 || 2", r.get(String.class));
    }
}
