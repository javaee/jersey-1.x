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

package com.sun.ws.rest.impl.bean;

import com.sun.ws.rest.api.Entity;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class ByteArrayRepresentationTest extends AbstractBeanTester {
    
    public ByteArrayRepresentationTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class Resource { 
        @HttpMethod("POST")
        public byte[] doPost(Entity<byte[]> in) {
            assertEquals("CONTENT", new String(in.getContent()));
            return "CONTENT".getBytes();
        }
    }
    
    public void testPut() {
        HttpResponseContext response = call(Resource.class, "POST", "/", null, null, "CONTENT");
        byte[] r = (byte[])response.getResponse().getEntity();
        assertEquals("CONTENT", new String(r));
    }
}
