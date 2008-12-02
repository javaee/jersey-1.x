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

package com.sun.jersey.json.impl;

import com.sun.jersey.api.json.JSONJAXBContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JSONMarshallerTest extends TestCase {
    
    Marshaller marshaller;
    static final Collection<Boolean> booleanVals = new HashSet<Boolean>(2){{add(Boolean.FALSE); add(Boolean.TRUE);}};
    static final Collection<Collection<String>> arrayVals = new HashSet<Collection<String>>(2) {{
            add(new HashSet<String>());
            add(new HashSet<String>(2){{
                add("two");
                add("one");
            }});
        }};

    
    public JSONMarshallerTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() {
        try {
            JSONJAXBContext context = new JSONJAXBContext();
            marshaller = context.createMarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(JSONMarshallerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testJsonEnabledProperty() throws Exception {
        checkValues(JSONJAXBContext.JSON_ENABLED, booleanVals);
    }
    
    public void testJsonNotationProperty() throws Exception {
        Collection<JSONJAXBContext.JSONNotation> notations = new LinkedList<JSONJAXBContext.JSONNotation>();
        for (JSONJAXBContext.JSONNotation notation : JSONJAXBContext.JSONNotation.values()) {
            notations.add(notation);
        }
        checkValues(JSONJAXBContext.JSON_NOTATION, notations);
    }
    
    public void testJsonRootUnwrappingProperty() throws Exception {
        checkValues(JSONJAXBContext.JSON_ROOT_UNWRAPPING, booleanVals);
    }

    public void testJsonArraysProperty() throws Exception {
        checkValues(JSONJAXBContext.JSON_ARRAYS, arrayVals);
    }

    public void testJsonNonstringsProperty() throws Exception {
        checkValues(JSONJAXBContext.JSON_NON_STRINGS, arrayVals);
    }
    
    public void testJsonNs2JnsProperty() throws Exception {
        Collection<Map<String, String>> vals = new HashSet<Map<String, String>>(2) {{
            add(new HashMap<String,String>());
            add(new HashMap<String, String>(1){{
                put("http://sun.com", "sc");
            }});
        }};
        checkValues(JSONJAXBContext.JSON_XML2JSON_NS, vals);
    }

    private void checkValues(String propertyName, Collection values) throws Exception {
        for (Object v : values) {
            marshaller.setProperty(propertyName, v);
            Object got = marshaller.getProperty(propertyName);
            assertEquals(v, got);
        }
    }
}
