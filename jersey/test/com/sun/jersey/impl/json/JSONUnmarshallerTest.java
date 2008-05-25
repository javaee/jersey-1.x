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

package com.sun.jersey.impl.json;

import com.sun.jersey.api.json.JSONJAXBContext;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JSONUnmarshallerTest extends TestCase {
    
    Unmarshaller unmarshaller;
    
    public JSONUnmarshallerTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() {
        try {
            JSONJAXBContext context = new JSONJAXBContext();
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(JSONUnmarshallerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testJsonEnabledProperty() throws Exception {
        Boolean[] vals = {Boolean.FALSE, Boolean.TRUE};
        checkValues(JSONJAXBContext.JSON_ENABLED, vals);
    }
    
    public void testJsonNotationProperty() throws Exception {
        Collection<String> notations = new LinkedList<String>();
        for (JSONJAXBContext.JSONNotation notation : JSONJAXBContext.JSONNotation.values()) {
            notations.add(notation.name());
        }
        checkValues(JSONJAXBContext.JSON_NOTATION, notations.toArray());
    }
    
    public void testJsonRootUnwrappingProperty() throws Exception {
        Boolean[] vals = {Boolean.FALSE, Boolean.TRUE};
        checkValues(JSONJAXBContext.JSON_ROOT_UNWRAPPING, vals);
    }

    public void testJsonNs2JnsProperty() throws Exception {
        String[] vals = {"{}", "{\"http:\\/\\/sun.com\":\"sc\"}"};
        checkValues(JSONJAXBContext.JSON_XML2JSON_NS, vals);
    }

    private void checkValues(String propertyName, Object[] values) throws Exception {
        for (Object v : values) {
            unmarshaller.setProperty(propertyName, v);
            Object got = unmarshaller.getProperty(propertyName);
            //System.out.println((new Formatter()).format("%s\n%s", v.toString(), got.toString()).toString());
            assertEquals(v, got);
        }
    }
}
