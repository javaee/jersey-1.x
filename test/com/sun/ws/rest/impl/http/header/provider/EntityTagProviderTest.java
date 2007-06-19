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

import com.sun.ws.rest.impl.provider.header.EntityTagProvider;
import javax.ws.rs.core.EntityTag;
import junit.framework.*;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class EntityTagProviderTest extends TestCase {
    
    public EntityTagProviderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of toString method, of class com.sun.ws.rest.impl.http.header.provider.EntityTagProvider.
     */
    public void testToString() {
        System.out.println("toString");
        
        EntityTag header = new EntityTag("Hello \"World\"", true);
        EntityTagProvider instance = new EntityTagProvider();
        
        String expResult = "W/\"Hello \\\"World\\\"\"";
        String result = instance.toString(header);
        assertEquals(expResult, result);
    }

    /**
     * Test of fromString method, of class com.sun.ws.rest.impl.http.header.provider.EntityTagProvider.
     */
    public void testFromString() throws Exception {
        System.out.println("fromString");
        
        String header = "W/\"Hello \\\"World\\\"\"";
        EntityTagProvider instance = new EntityTagProvider();
        
        EntityTag expResult = new EntityTag("Hello \"World\"", true);
        EntityTag result = instance.fromString(header);
        assertEquals(expResult, result);
    }
    
}
