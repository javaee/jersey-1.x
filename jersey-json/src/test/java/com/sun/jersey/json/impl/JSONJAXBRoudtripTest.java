/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.api.json.JSONUnmarshaller;
import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class JSONJAXBRoudtripTest extends TestCase {
    
    private static final String PKG_NAME = "com/sun/jersey/json/impl/";

    
    Collection<Object> beans;
    Class[] classes;
    

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        String beanClasses = JSONTestHelper.getResourceAsString(PKG_NAME, "jaxb.index");
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
        allBeansTest(beans);
    }

    public void testDefaultConfigOld() throws Exception {
        System.out.println("DEFAULT CONFIG OLD");
        allBeansTest(beans, true);
    }
    
    public void testInternalNotation() throws Exception {
        System.out.println("INTERNAL NOTATION");
        allBeansTest(JSONConfiguration.mapped().rootUnwrapping(false).build(), beans);
    }

    public void testInternalNotationOld() throws Exception {
        System.out.println("INTERNAL NOTATION OLD");
        allBeansTest(JSONConfiguration.mapped().rootUnwrapping(false).build(), beans, true);
    }

    public void testInternalNotationDeprecatedConfig() throws Exception {
        System.out.println("INTERNAL NOTATION DEPRECATED CONFIG");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
        props.put(JSONJAXBContext.JSON_ARRAYS, new HashSet<String>(1){{add("list");}});
        props.put(JSONJAXBContext.JSON_NON_STRINGS, new HashSet<String>(1){{add("b");}});
        allBeansTest(props, beans);
    }

    public void testInternalNotationDeprecatedConfigOld() throws Exception {
        System.out.println("INTERNAL NOTATION DEPRECATED CONFIG OLD");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
        allBeansTest(props, beans, true);
    }

    public void testInternalNotationAttrAsElems() throws Exception {
        System.out.println("INTERNAL NOTATION WITH SOME ATTR AS ELEMS");
        allBeansTest(JSONConfiguration.mapped().rootUnwrapping(true).attributeAsElement("i", "j").build(), beans);
    }

    public void testInternalNotationAttrAsElemsOld() throws Exception {
        System.out.println("INTERNAL NOTATION WITH SOME ATTR AS ELEMS OLD");
        allBeansTest(JSONConfiguration.mapped().rootUnwrapping(true).attributeAsElement("i", "j").build(), beans, true);
    }

    public void testInternalNotationAttrAsElemsDeprecatedConfig() throws Exception {
        System.out.println("INTERNAL NOTATION WITH SOME ATTR AS ELEMS DEPRECATED CONFIG");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);
        props.put(JSONJAXBContext.JSON_ATTRS_AS_ELEMS, new HashSet<String>(2){{add("i");add("j");}});
        props.put(JSONJAXBContext.JSON_ARRAYS, new HashSet<String>(1){{add("list");}});
        props.put(JSONJAXBContext.JSON_NON_STRINGS, new HashSet<String>(1){{add("b");}});
        allBeansTest(props, beans);
    }

    public void testInternalNotationAttrAsElemsDeprecatedConfigOld() throws Exception {
        System.out.println("INTERNAL NOTATION WITH SOME ATTR AS ELEMS DEPRECATED CONFIG OLD");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);
        props.put(JSONJAXBContext.JSON_ATTRS_AS_ELEMS, new HashSet<String>(2){{add("i");add("j");}});
        allBeansTest(props, beans, true);
    }

    public void testJettisonBadgerfishNotation() throws Exception {
        System.out.println("BADGERFISH NOTATION");
        allBeansTest(JSONConfiguration.badgerFish().build(), beans);
    }

    public void testJettisonBadgerfishNotationDeprecatedConfig() throws Exception {
        System.out.println("BADGERFISH NOTATION DEPRECATED CONFIG");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.BADGERFISH);
        allBeansTest(props, beans);
    }

    public void testNaturalNotation() throws Exception {
        System.out.println("NATURAL NOTATION");
        allBeansTest(JSONConfiguration.natural().build(), beans);
    }

    public void testNaturalNotationFormatted() throws Exception {
        System.out.println("NATURAL NOTATION FORMATTED");
        allBeansTest(JSONConfiguration.natural().humanReadableFormatting(true).build(), beans);
    }


    public void testNaturalNotationDeprecatedConfig() throws Exception {
        System.out.println("NATURAL NOTATION DEPRECATED CONFIG");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.NATURAL);
        allBeansTest(props, beans);
    }

    public void testJettisonMappedNotation() throws Exception {
        System.out.println("MAPPED (JETTISON) NOTATION");
        Map<String, Object> props = new HashMap<String, Object>();
        allBeansTest(JSONConfiguration.mappedJettison().build(), beans);
    }

    public void testJettisonMappedNotationDeprecatedConfig() throws Exception {
        System.out.println("MAPPED (JETTISON) NOTATION DEPRECATED CONFIG");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED_JETTISON");
        allBeansTest(props, beans);
    }

    public synchronized void allBeansTest(final Collection<Object> beans) throws Exception {
        allBeansTest(beans, false);
    }

    public synchronized void allBeansTest(final Collection<Object> beans, final boolean useDefaultConfiguration) throws Exception {
        allBeansTest(JSONConfiguration.DEFAULT, Collections.<String, Object>emptyMap(), beans, useDefaultConfiguration);
    }

    public synchronized void allBeansTest(JSONConfiguration configuration,
                                          final Collection<Object> beans) throws Exception {
        allBeansTest(configuration, beans, false);
    }

    public synchronized void allBeansTest(JSONConfiguration configuration,
                                          final Collection<Object> beans,
                                          final boolean useDefaultConfiguration) throws Exception {
        allBeansTest(configuration, Collections.<String, Object>emptyMap(), beans, useDefaultConfiguration);
    }

    public synchronized void allBeansTest(final Map<String, Object> properties,
                                          final Collection<Object> beans) throws Exception {
        allBeansTest(JSONConfiguration.DEFAULT, properties, beans, false);
    }

    public synchronized void allBeansTest(final Map<String, Object> properties,
                                          final Collection<Object> beans,
                                          final boolean useDefaultConfiguration) throws Exception {
        allBeansTest(JSONConfiguration.DEFAULT, properties, beans, useDefaultConfiguration);
    }
    
    public synchronized void allBeansTest(JSONConfiguration configuration,
                                          final Map<String, Object> properties,
                                          final Collection<Object> beans,
                                          final boolean useDefaultConfiguration) throws Exception {
        for (Object originalBean : beans) {
            if (!useDefaultConfiguration && JSONConfiguration.Notation.MAPPED.equals(configuration.getNotation())) {
                final JSONConfiguration.MappedBuilder builder
                        = (JSONConfiguration.MappedBuilder) JSONConfiguration.copyBuilder(configuration);

                builder.arrays(getArrayElements(originalBean));
                builder.nonStrings(getNonStringElements(originalBean));

                configuration = builder.build();
            }

            final Class<? extends Object> beanClass = originalBean.getClass();
            final Class<?>[] classesToBeBound = {beanClass};
            JSONJAXBContext context = properties.isEmpty() ?
                    new JSONJAXBContext(configuration, classesToBeBound) : new JSONJAXBContext(classesToBeBound, properties);

            JSONMarshaller marshaller = context.createJSONMarshaller();
            JSONUnmarshaller unmarshaller = context.createJSONUnmarshaller();

            printAsXml(originalBean);

            StringWriter sWriter = new StringWriter();
            marshaller.marshallToJSON(originalBean, sWriter);

            System.out.println(sWriter.toString());
            final Object actual = unmarshaller.unmarshalFromJSON(new StringReader(sWriter.toString()), beanClass);

            if (useDefaultConfiguration) {
                // let know the bean that the old approach (instance with empty properties -> 'null') is being tested so it should
                // handle the equals method a little bit different
                try {
                    final Method useOldApproachMethod = beanClass.getMethod("setUseOldApproach", boolean.class);
                    useOldApproachMethod.invoke(originalBean, true);
                    useOldApproachMethod.invoke(actual, true);
                } catch (Exception e) {
                    // Ignore this - if this invocation fails the test will fail as well.
                }
            }

            assertEquals(originalBean, actual);
        }
    }

    private String[] getArrayElements(final Object originalBean) {
        return getBeanElements(originalBean, "getArrayElements");
    }

    private String[] getNonStringElements(final Object originalBean) {
        return getBeanElements(originalBean, "getNonStringElements");
    }

    private String[] getBeanElements(final Object originalBean, final String methodToInvoke) {
        try {
            final Method method = originalBean.getClass().getDeclaredMethod(methodToInvoke);
            Object arrays = method.invoke(null);

            return arrays instanceof String[] ? (String[]) arrays : new String[0];
        } catch (Exception e) {
            return new String[0];
        }
    }

    private void printAsXml(Object originalBean) throws JAXBException, PropertyException {
        System.out.println("Checking " + originalBean.toString());
        JAXBContext ctx = JAXBContext.newInstance(originalBean.getClass());
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(originalBean, System.out);
    }
}
