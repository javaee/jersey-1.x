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
package com.sun.jersey.impl.json;

import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.impl.json.JSONUnmarshaller;
import com.sun.jersey.impl.json.JSONMarshaller;
import com.sun.jersey.impl.test.util.TestHelper;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JSONJAXBRoudtripTest extends TestCase {
    
    private static final String PKG_NAME = "com/sun/jersey/impl/json/";

    
    Collection<Object> beans;
    Class[] classes;
    

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        String beanClasses = TestHelper.getResourceAsString(PKG_NAME, "jaxb.index");
        Collection classCollection = new LinkedList<Class>();
        StringTokenizer tokenizer = new StringTokenizer(beanClasses);
        //StringTokenizer tokenizer = new StringTokenizer("SimpleBeanWithAttributes");//beanClasses);
        beans = new LinkedList<Object>();
        while (tokenizer.hasMoreTokens()) {
            String className = tokenizer.nextToken();
            if (!"".equals(className)) {
                Class beanClass = Class.forName(PKG_NAME.replace('/', '.') + className);
                classCollection.add(beanClass);
                Method testBeanCreator = beanClass.getDeclaredMethod("createTestInstance");
                Object testBean = testBeanCreator.invoke(null);
                beans.add(testBean);
            }
        }
        classes = (Class[]) classCollection.toArray(new Class[0]);
    }
    
    public void testDefaultConfig() throws Exception {
        System.out.println("DEFAULT CONFIG");
        allBeansTest(new JSONJAXBContext(classes), beans);
    }
    
    public void testInternalNotation() throws Exception {
        System.out.println("INTERNAL NOTATION");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
        allBeansTest(new JSONJAXBContext(classes, props), beans);
    }

    public void testJettisonBadgerfishNotation() throws Exception {
        System.out.println("BADGERFISH NOTATION");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "BADGERFISH");
        allBeansTest(new JSONJAXBContext(classes, props), beans);
    }
    
//    TODO: Jettison gets stuck on the following :-(
//    public void testJettisonMappedNotation() throws Exception {
//        System.out.println("MAPPED (JETTISON) NOTATION");
//        Map<String, Object> props = new HashMap<String, Object>();
//        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED_JETTISON");
//        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);        
//        allBeansTest(new JSONJAXBContext(classes, props), beans);
//    }
    
    public synchronized void allBeansTest(JSONJAXBContext context, Collection<Object> beans) throws Exception {
        JSONMarshaller marshaller = (JSONMarshaller)context.createMarshaller();
        marshaller.setProperty(JSONJAXBContext.JSON_ENABLED, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        JSONUnmarshaller unmarshaller = (JSONUnmarshaller)context.createUnmarshaller();
        unmarshaller.setProperty(JSONJAXBContext.JSON_ENABLED, Boolean.TRUE);
        for (Object originalBean : beans) {
            System.out.println("Checking " + originalBean.toString());
            JAXBContext ctx = JAXBContext.newInstance(originalBean.getClass());
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(originalBean, System.out);
            StringWriter sWriter = new StringWriter();
            marshaller.marshal(originalBean, sWriter);
            System.out.println(sWriter.toString());
            assertEquals(originalBean, unmarshall(unmarshaller, originalBean.getClass(), new StringReader(sWriter.toString())));
            System.out.println("OK");
        }
    }

    @SuppressWarnings("unchecked")
    private Object unmarshall(JSONUnmarshaller unmarshaller, Class type, Reader r) throws Exception {
        assert null != unmarshaller;
        JAXBElement jaxbElem = (JAXBElement)unmarshaller.unmarshal(r, type);
        return jaxbElem.getValue();
    }
    
}
