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

package com.sun.jersey.impl.http.header.provider;

import com.sun.jersey.core.impl.provider.header.MediaTypeProvider;
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

    public void testToString() {
        MediaType header = new MediaType("application", "xml");
        MediaTypeProvider instance = new MediaTypeProvider();
        
        String expResult = "application/xml";
        String result = instance.toString(header);
        assertEquals(expResult, result);
    }
    
    public void testToStringWithParams() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("charset", "utf8");
        MediaType header = new MediaType("application", "xml", params);
        MediaTypeProvider instance = new MediaTypeProvider();
        
        String expResult = "application/xml;charset=utf8";
        String result = instance.toString(header);
        assertEquals(expResult, result);
    }

    public void testFromString() throws Exception {
        MediaTypeProvider instance = new MediaTypeProvider();
        
        String header = "application/xml";
        MediaType result = instance.fromString(header);
        assertEquals(result.getType(), "application");
        assertEquals(result.getSubtype(), "xml");
        assertEquals(result.getParameters().size(), 0);
    }
    
    public void testFromStringWithParams() throws Exception {
        String header = "application/xml;charset=utf8";
        MediaTypeProvider instance = new MediaTypeProvider();
        
        MediaType result = instance.fromString(header);
        assertEquals(result.getType(), "application");
        assertEquals(result.getSubtype(), "xml");
        assertEquals(result.getParameters().size(), 1);
        assertTrue(result.getParameters().containsKey("charset"));
        assertEquals(result.getParameters().get("charset"), "utf8");
    }
    
    public void testWithQuotedParam() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("foo", "\"bar\"");
        MediaType header = new MediaType("application", "xml", params);
        MediaTypeProvider instance = new MediaTypeProvider();
        
        String result = instance.toString(header);
        String expResult = "application/xml;foo=\"\\\"bar\\\"\"";
        assertEquals(expResult, result);
        
        MediaType m = instance.fromString(result);
        assertEquals("\"bar\"", m.getParameters().get("foo"));
    }    
}