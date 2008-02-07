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

package com.sun.ws.rest.impl.entity;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.api.client.ResourceProxy;
import com.sun.ws.rest.api.client.ClientResponse;
import javax.ws.rs.POST;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractTypeTester extends AbstractResourceTester {
    
    protected AbstractTypeTester(String testName) {
        super(testName);
    }
    
    public static abstract class AResource<T> {
        @POST
        public T post(T t) {
            return t;
        }
    }

    protected <T> void _test(Class<T> typeT, T in, Class resource) {
        _test(typeT, in, resource, true);
    }
    
    protected <T> void _test(Class<T> typeT, T in, Class resource, boolean verify) {
        initiateWebApplication(resource);
        ResourceProxy r = resourceProxy("/");

        ClientResponse rib = r.post(ClientResponse.class, in);
        
        byte[] inBytes = (byte[])
                rib.getProperties().get("request.entity");
        byte[] outBytes = (byte[])
                rib.getProperties().get("response.entity");
        
        if (verify) _verify(inBytes, outBytes);
    }
    
    protected void _verify(byte[] in, byte[] out) {
        assertEquals(in.length, out.length);
        boolean e = false;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != out[i])
                assertEquals("Index: " + i, in[i], out[i]);
        }
    }
}
