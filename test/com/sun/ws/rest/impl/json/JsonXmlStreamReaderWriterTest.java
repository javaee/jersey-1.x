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

import com.sun.ws.rest.impl.json.writer.*;
import com.sun.ws.rest.impl.json.reader.JsonXmlStreamReader;
import com.sun.ws.rest.impl.test.util.TestHelper;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JsonXmlStreamReaderWriterTest extends TestCase {

    private static final String PKG_NAME = "com/sun/ws/rest/impl/json/";
    
    private static User john = new User("john", "John White", "passwd123");
    private static User bob = new User("bob", "Bob Black", "312dwssap");

    JAXBContext jaxbContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jaxbContext = JAXBContext.newInstance("com.sun.ws.rest.impl.json");
    }
    
    public void testSimpleBeanUnwrapped() throws JAXBException, IOException {
        tryBean(john, "userWrapped.json", false);
    }
    
    public void testSimpleBeanWrapped() throws JAXBException, IOException {    
        tryBean(john, "userUnwrapped.json", true);
    }
    
    public void testjMakiTableOneUser() throws JAXBException, IOException {
        List<User> users = new LinkedList<User>();
        users.add(john);
        tryBean(new UserTable(users), "userTableWrappedWithOneUser.json", false, "rows", null);
    }
    
    
    public void testjMakiTableTwoUsersWrapped() throws JAXBException, IOException {
        List<User> users = new LinkedList<User>();
        users.add(john);
        users.add(bob);
        tryBean(new UserTable(users),"userTableWrappedWithTwoUsers.json", false);
    }
    
    public void testjMakiTableTwoUsersUnwrapped() throws JAXBException, IOException {
        List<User> users = new LinkedList<User>();
        users.add(john);
        users.add(bob);
        tryBean(new UserTable(users),"userTableUnwrappedWithTwoUsers.json", true);
    }

    public void testTreeModel() throws JAXBException, IOException {
        TreeModel treeModel = new TreeModel(new TreeModel.Node("node1"));
        treeModel.root.children = new LinkedList<TreeModel.Node>();
        treeModel.root.expanded = true;
        treeModel.root.children.add(new TreeModel.Node("child1"));
        treeModel.root.children.add(new TreeModel.Node("child2"));
        treeModel.root.children.add(new TreeModel.Node("child3"));
        tryBean(treeModel, "oneLevelTree.json", true, null, "expanded");
    }
    
    public void testSimpleBeanWithAttributes() throws JAXBException, IOException {
        SimpleBeanWithAttributes bean = (SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance();
        tryBean(bean, "simpleBeanWithAttributes.json", true, null, null);
    }

    public void testComplexBeanWithAttributes() throws JAXBException, IOException {
        ComplexBeanWithAttributes bean = (ComplexBeanWithAttributes)ComplexBeanWithAttributes.createTestInstance();
        tryBean(bean, "complexBeanWithAttributes.json", true, null, null);
    }

    public void testEmptyListWrapper() throws JAXBException, IOException {
        ListWrapperBean bean = (ListWrapperBean)ListWrapperBean.createTestInstance();
        tryBean(bean, "emptyListWrapper.json", false, null, null);
    }
    
    public void testTwoListsWrapper() throws JAXBException, IOException {
        TwoListsWrapperBean bean = (TwoListsWrapperBean)TwoListsWrapperBean.createTestInstance();
        tryBean(bean, "twoListsWrapper.json", false, "property1, property2", null);
    }

    public void testListAndNonList() throws JAXBException, IOException {
        ListAndNonListBean bean = (ListAndNonListBean)ListAndNonListBean.createTestInstance();
        tryBean(bean, "listAndNonList.json", true, "a", null);
    }
    
//    public void testWrittingAttrs() throws Exception {
//        SimpleBeanWithAttributes bean = (SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance();
//        tryWritingBean(bean, "simpleBeanWithAttributes.json", true, null, null);
//    }
//
//    public void testReadingAttrs() throws Exception {
//        SimpleBeanWithAttributes bean = (SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance();
//        System.out.println("testing: " + bean.toString());
//        tryReadingBean("simpleBeanWithAttributes.json", bean, true, null, null);
//    }
    
    public void tryBean(Object jaxbBean, String filename, boolean stripRoot) throws JAXBException, IOException {
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
            boolean stripRoot, String arrays, String nonStrings) throws JAXBException, IOException {
        Collection<String> arrayElements = new LinkedList<String>();
        Collection<String> nonStringElements = new LinkedList<String>();
        addStringsToCollection(arrays, arrayElements);
        addStringsToCollection(nonStrings, nonStringElements);
        tryWritingBean(jaxbBean, filename, stripRoot, arrayElements, nonStringElements);
        tryReadingBean(filename, jaxbBean, stripRoot, arrayElements, nonStringElements);
    }

    public void tryWritingBean(Object jaxbBean, String expectedJsonExprFilename, 
            boolean stripRoot, Collection<String> arrays, Collection<String> nonStrings) throws JAXBException, IOException {
        String expectedJsonExpr = TestHelper.getResourceAsString(PKG_NAME, expectedJsonExprFilename);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter resultWriter = new StringWriter();
        marshaller.marshal(jaxbBean, new JsonXmlStreamWriter(resultWriter, stripRoot, arrays, nonStrings));
        assertEquals("MISMATCH:\n" + expectedJsonExpr + "\n" + resultWriter.toString() + "\n", 
                expectedJsonExpr, resultWriter.toString());
    }

    public void tryReadingBean(String jsonExprFilename, Object expectedJaxbBean, 
            boolean stripRoot, Collection<String> arrays, Collection<String> nonStrings) throws JAXBException, IOException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement jaxbElement = unmarshaller.unmarshal(
                new JsonXmlStreamReader(
                    new StringReader(TestHelper.getResourceAsString(PKG_NAME, jsonExprFilename)), stripRoot),
                expectedJaxbBean.getClass());
        System.out.println("unmarshalled: " + jaxbElement.getValue().toString());
        assertEquals("MISMATCH:\n" + expectedJaxbBean + "\n" + jaxbElement.getValue() + "\n", 
                expectedJaxbBean, jaxbElement.getValue());
    }
}
