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

package com.sun.ws.rest.impl;

import com.sun.ws.rest.impl.application.WebApplicationContext;
import com.sun.jersey.spi.container.ContainerRequest;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriPathHttpRequestTest extends TestCase {
    
    public UriPathHttpRequestTest(String testName) {
        super(testName);
    }
    
    public void testGeneral() throws Exception {
        ContainerRequest r = new TestHttpRequestContext(null, "GET", null,
                "/context/widgets/10", "/context/");
        UriInfo ui = new WebApplicationContext(null, r, null);
        assertEquals("widgets/10", ui.getPath());
        assertEquals("widgets/10", ui.getPath(true));
        assertEquals("widgets/10", ui.getPath(false));
    }    
    
    public void testEncoded() throws Exception {
        ContainerRequest r = new TestHttpRequestContext(null, "GET", null,
                "/context/widgets%20/%2010", "/context/");
        UriInfo ui = new WebApplicationContext(null, r, null);
        assertEquals("widgets / 10", ui.getPath());
        assertEquals("widgets / 10", ui.getPath(true));
        assertEquals("widgets%20/%2010", ui.getPath(false));
    }    
}
