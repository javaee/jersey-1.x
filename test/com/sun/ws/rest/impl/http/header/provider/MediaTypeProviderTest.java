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

package com.sun.ws.rest.impl.http.header.provider;

import com.sun.ws.rest.impl.provider.header.MediaTypeProvider;
import java.util.HashMap;
import junit.framework.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class MediaTypeProviderTest extends TestCase {
    
    public MediaTypeProviderTest(String testName) {
        super(testName);
    }

    /**
     * Test of toString method, of class com.sun.ws.rest.impl.http.header.provider.MediaTypeProvider.
     */
    public void testToString() {
        System.out.println("toString");
        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("charset","utf8");
        MediaType header = new MediaType("application","xml",params);
        MediaTypeProvider instance = new MediaTypeProvider();
        
        String expResult = "application/xml;charset=utf8";
        String result = instance.toString(header);
        assertEquals(expResult, result);
    }

    /**
     * Test of fromString method, of class com.sun.ws.rest.impl.http.header.provider.MediaTypeProvider.
     */
    public void testFromString() throws Exception {
        System.out.println("fromString");
        
        String header = "application/xml;charset=utf8";
        MediaTypeProvider instance = new MediaTypeProvider();
        
        MediaType result = instance.fromString(header);
        assertEquals(result.getType(),"application");
        assertEquals(result.getSubtype(),"xml");
        assertEquals(result.getParameters().size(),1);
        assertTrue(result.getParameters().containsKey("charset"));
        assertEquals(result.getParameters().get("charset"),"utf8");
        header = "application/xml";
        result = instance.fromString(header);
        assertEquals(result.getType(),"application");
        assertEquals(result.getSubtype(),"xml");
        assertEquals(result.getParameters().size(),0);
    }
    
}
