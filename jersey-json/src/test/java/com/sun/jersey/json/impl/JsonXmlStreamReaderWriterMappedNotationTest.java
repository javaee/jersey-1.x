/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONConfiguration.MappedBuilder;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
import com.sun.jersey.json.impl.writer.JsonXmlStreamWriter;

import static com.sun.jersey.json.impl.util.JSONNormalizer.normalizeJsonString;

import junit.framework.TestCase;

/**
 * {@code JsonXmlStreamReader} test for JSON in the mapped notation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class JsonXmlStreamReaderWriterMappedNotationTest extends TestCase {

    private static final String PKG_NAME = "com/sun/jersey/json/impl/";
    
    private static User john = new User("john", "John White", "passwd123");
    private static User bob = new User("bob", "Bob Black", "312dwssap");

    JAXBContext jaxbContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jaxbContext = JAXBContext.newInstance("com.sun.jersey.json.impl");
    }
    
    public void testSimpleBeanWrapped() throws Exception {
        tryBean(john, "userWrapped.json", false);
    }
    
    public void testSimpleBeanUnwrapped() throws Exception {
        tryBean(john, "userUnwrapped.json", true);
    }
    
    public void testjMakiTableOneUser() throws Exception {
        List<User> users = new LinkedList<User>();
        users.add(john);
        tryBean(new UserTable(users), "userTableWrappedWithOneUser.json", false, "rows", null);
    }
    
    
    public void testjMakiTableTwoUsersWrapped() throws Exception {
        List<User> users = new LinkedList<User>();
        users.add(john);
        users.add(bob);
        tryBean(new UserTable(users),"userTableWrappedWithTwoUsers.json", false);
    }
    
    public void testjMakiTableTwoUsersUnwrapped() throws Exception {
        List<User> users = new LinkedList<User>();
        users.add(john);
        users.add(bob);
        tryBean(new UserTable(users),"userTableUnwrappedWithTwoUsers.json", true);
    }

    public void testTreeModel() throws Exception {
        TreeModel treeModel = new TreeModel(new TreeModel.Node("node1"));
        treeModel.root.children = new LinkedList<TreeModel.Node>();
        treeModel.root.expanded = true;
        treeModel.root.children.add(new TreeModel.Node("child1"));
        treeModel.root.children.add(new TreeModel.Node("child2"));
        treeModel.root.children.add(new TreeModel.Node("child3"));
        tryBean(treeModel, "oneLevelTree.json", true, null, "expanded");
    }

    public void testSimpleBeanEmpty() throws Exception {
        tryBean(new SimpleBean(), "simpleBeanEmpty.json", true, null, null);
    }
    
    public void testSimpleBeanWithAttributes() throws Exception {
        SimpleBeanWithAttributes bean = JSONTestHelper.createTestInstance(SimpleBeanWithAttributes.class);
        tryBean(bean, "simpleBeanWithAttributes.json", true, null, null);
    }

    public void testSimpleBeanWithObjectAttributes() throws Exception {
        SimpleBeanWithObjectAttributes bean = JSONTestHelper.createTestInstance(SimpleBeanWithObjectAttributes.class);
        tryBean(bean, "simpleBeanWithObjectAttributes.json", true, null, null);
    }

    public void testSimpleBeanWithAttributesAsElems() throws Exception {
        SimpleBeanWithAttributes bean = JSONTestHelper.createTestInstance(SimpleBeanWithAttributes.class);
        Collection<String> attrAsElems = new LinkedList<String>();
        addStringsToCollection("i", attrAsElems);
        addStringsToCollection("j", attrAsElems);
        //tryWritingBean(bean, "simpleBeanWithAttributesAsElems.json", true, null, null, attrAsElems);
        tryBean(bean, "simpleBeanWithAttributesAsElems.json", true, null, null, "i j");
    }

    public void testSimpleBeanWithJustOneAttribute() throws Exception {
        SimpleBeanWithJustOneAttribute bean = JSONTestHelper.createTestInstance(SimpleBeanWithJustOneAttribute.class);
        tryBean(bean, "simpleBeanWithJustOneAttribute.json", true, null, null);
    }

    public void testSimpleBeanWithJustOneAttributeAsElem() throws Exception {
        SimpleBeanWithJustOneAttribute bean = JSONTestHelper.createTestInstance(SimpleBeanWithJustOneAttribute.class);
        Collection<String> attrAsElems = new LinkedList<String>();
        addStringsToCollection("uri", attrAsElems);
        //tryWritingBean(bean, "simpleBeanWithJustOneAttributeAsElem.json", true, null, null, attrAsElems);
        tryBean(bean, "simpleBeanWithJustOneAttributeAsElem.json", true, null, null, "uri");
    }

    public void testSimpleBeanWithJustOneAttributeAndValue() throws Exception {
        SimpleBeanWithJustOneAttributeAndValue bean = JSONTestHelper.createTestInstance(SimpleBeanWithJustOneAttributeAndValue.class);
        tryBean(bean, "simpleBeanWithJustOneAttributeAndValue.json", true, null, null);
    }

    public void testSimpleBeanWithJustOneAttributeAsElemAndValue() throws Exception {
        SimpleBeanWithJustOneAttributeAndValue bean = JSONTestHelper.createTestInstance(SimpleBeanWithJustOneAttributeAndValue.class);
        Collection<String> attrAsElems = new LinkedList<String>();
        addStringsToCollection("uri", attrAsElems);
        //tryWritingBean(bean, "simpleBeanWithJustOneAttributeAsElemAndValue.json", true, null, null, attrAsElems);
        tryBean(bean, "simpleBeanWithJustOneAttributeAsElemAndValue.json", true, null, null, "uri");
    }

    public void testComplexBeanWithAttributes() throws Exception {
        ComplexBeanWithAttributes bean = JSONTestHelper.createTestInstance(ComplexBeanWithAttributes.class);
        tryBean(bean, "complexBeanWithAttributes.json", true, null, null);
    }

    public void testEmptyComplexBeanWithAttributes() throws Exception {
        ComplexBeanWithAttributes4 bean = JSONTestHelper.createTestInstance(ComplexBeanWithAttributes4.class);
        tryBean(bean, "complexBeanWithAttributes4.json", true, "list", "b");
    }

    public void testEmptyListWrapper() throws Exception {
        ListWrapperBean bean = JSONTestHelper.createTestInstance(ListWrapperBean.class);
        tryBean(bean, "emptyListWrapper.json", false, null, "item");
    }
    
    public void testTwoListsWrapper() throws Exception {
        TwoListsWrapperBean bean = JSONTestHelper.createTestInstance(TwoListsWrapperBean.class);
        tryBean(bean, "twoListsWrapper.json", false, "property1, property2", null);
    }

    public void testListAndNonList() throws Exception {
        ListAndNonListBean bean = JSONTestHelper.createTestInstance(ListAndNonListBean.class);
        tryBean(bean, "listAndNonList.json", true, "a", null);
    }
    
    public void testPureCharDataValue() throws Exception {
        PureCharDataBean bean = JSONTestHelper.createTestInstance(PureCharDataBean.class);
        tryBean(bean, "pureCharDataValue.json", true, null, null);
    }

    public void testAttrAndCharDataValue() throws Exception {
        AttrAndCharDataBean bean = JSONTestHelper.createTestInstance(AttrAndCharDataBean.class);
        tryBean(bean, "attrAndCharDataValue.json", true, null, null);
    }
    
    public void testAttrAndXmlVal() throws Exception {
        SimpleBeanWithAttributes bean = JSONTestHelper.createTestInstance(SimpleBeanWithAttributes.class);
        tryBean(bean, "simpleBeanWithAttributes.json", true, null, null);
    }

    public void testMultipleArrayElements() throws Exception {
        tryReadingBean("userTableVerbose.json", UserTable.createTestInstance(), JSONConfiguration.mapped().rootUnwrapping(true).build());
    }

    public void tryBean(Object jaxbBean, String filename, boolean stripRoot) throws Exception {
        tryBean(jaxbBean, filename, stripRoot, null, null);
    }

    private void addStringsToCollection(String strings, Collection<String> collection) {
        if ((null == strings) || (null == collection)) {
            return;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(strings);
        while (stringTokenizer.hasMoreElements()) {
            collection.add(stringTokenizer.nextToken());
        }
    }
    
    public void tryBean(Object jaxbBean, String filename, 
            boolean stripRoot, String arrays, String nonStrings) throws Exception {
        tryBean(jaxbBean, filename, stripRoot, arrays, nonStrings, null);
    }

    public void tryBean(Object jaxbBean, String filename,
            boolean stripRoot, String arrays, String nonStrings, String attrAsElems) throws Exception {
        Collection<String> arrayElements = new LinkedList<String>();
        Collection<String> nonStringElements = new LinkedList<String>();
        Collection<String> attrAsElements = new LinkedList<String>();
        addStringsToCollection(arrays, arrayElements);
        addStringsToCollection(nonStrings, nonStringElements);
        addStringsToCollection(attrAsElems, attrAsElements);
        final MappedBuilder configBuilder = JSONConfiguration.mapped().rootUnwrapping(stripRoot);
        for (String array : arrayElements) {
            configBuilder.arrays(array);
        }
        for (String nonString : nonStringElements) {
            configBuilder.nonStrings(nonString);
        }
        for (String attrAsElem : attrAsElements) {
            configBuilder.attributeAsElement(attrAsElem);
        }
        JSONConfiguration config = configBuilder.build();
        tryWritingBean(jaxbBean, filename, config);
        tryReadingBean(filename, jaxbBean, config);
    }

    public void tryWritingBean(Object jaxbBean, String expectedJsonExprFilename, 
            JSONConfiguration config) throws Exception {
        String expectedJsonExpr = JSONTestHelper.getResourceAsString(PKG_NAME, expectedJsonExprFilename);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter resultWriter = new StringWriter();
        marshaller.marshal(jaxbBean,
                JsonXmlStreamWriter.createWriter(resultWriter, config, JSONHelper.getRootElementName((Class<Object>) jaxbBean.getClass())));
        assertEquals("MISMATCH:\n" + expectedJsonExpr + "\n" + resultWriter.toString() + "\n", 
                normalizeJsonString(expectedJsonExpr), normalizeJsonString(resultWriter.toString()));
    }

    public void tryReadingBean(String jsonExprFilename, Object expectedJaxbBean, 
            JSONConfiguration config) throws JAXBException, IOException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement jaxbElement = null;
        try {
            jaxbElement = unmarshaller.unmarshal(
                    JsonXmlStreamReader.create(new StringReader(JSONTestHelper.getResourceAsString(PKG_NAME,
                            jsonExprFilename)), config, null, expectedJaxbBean.getClass(), jaxbContext, false),
                    expectedJaxbBean.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jaxbElement != null) {
            final Object jaxbElementValue = jaxbElement.getValue();
            System.out.println("unmarshalled: " + jaxbElementValue.toString());
            assertEquals("MISMATCH:\n" + expectedJaxbBean + "\n" + jaxbElementValue + "\n",
                    expectedJaxbBean, jaxbElementValue);
        } else {
            fail("JAXBElement is null.");
        }
    }
}
