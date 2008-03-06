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
package com.sun.ws.rest.impl.json;

import com.sun.ws.rest.impl.test.util.TestHelper;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JSONJAXBRoudtripTest extends TestCase {
    
    private static final String PKG_NAME = "com/sun/ws/rest/impl/json/";

    
    Collection<Object> beans;
    Class[] classes;
    

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        String beanClasses = TestHelper.getResourceAsString(PKG_NAME, "jaxb.index");
        StringTokenizer tokenizer = new StringTokenizer(beanClasses);
        Collection classCollection = new LinkedList<Class>();
        beans = new LinkedList<Object>();
        while (tokenizer.hasMoreTokens()) {
            String className = tokenizer.nextToken();
            Class beanClass = Class.forName(PKG_NAME.replace('/', '.') + className);
            classCollection.add(beanClass);
            beans.add(beanClass.newInstance());
        }
        classes = (Class[]) classCollection.toArray(new Class[0]);
    }
    
    public void testDefaultConfig() throws Exception {
        allBeansTest(new JSONJAXBContext(classes), beans);
    }
    
    public void testInternalNotation() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);        
        allBeansTest(new JSONJAXBContext(classes, props), beans);
    }

    public void testJettisonBadgerfishNotation() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "BADGERFISH");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);        
        allBeansTest(new JSONJAXBContext(classes, props), beans);
    }
    
    public void testJettisonMappedNotation() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED_JETTISON");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);        
        allBeansTest(new JSONJAXBContext(classes, props), beans);
    }
    
    public void allBeansTest(JSONJAXBContext context, Collection<Object> beans) throws Exception {
        Marshaller marshaller = context.createMarshaller();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        for (Object originalBean : beans) {
            //System.out.println("Checking " + originalBean.toString());
            StringWriter sWriter = new StringWriter();
            marshaller.marshal(originalBean, sWriter);
            assertEquals(
                    originalBean,
                    unmarshaller.unmarshal(new StringReader(sWriter.toString())));
        }
    }
}
