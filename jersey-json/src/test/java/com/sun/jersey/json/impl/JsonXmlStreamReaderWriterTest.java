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

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONConfiguration.MappedBuilder;
import com.sun.jersey.json.impl.writer.JsonXmlStreamWriter;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
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

    private static final String PKG_NAME = "com/sun/jersey/json/impl/";
    
    private static User john = new User("john", "John White", "passwd123");
    private static User bob = new User("bob", "Bob Black", "312dwssap");

    JAXBContext jaxbContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jaxbContext = JAXBContext.newInstance("com.sun.jersey.json.impl");
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

    public void testSimpleBeanWithAttributesAsElems() throws Exception {
        SimpleBeanWithAttributes bean = (SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance();
        Collection<String> attrAsElems = new LinkedList<String>();
        addStringsToCollection("i", attrAsElems);
        addStringsToCollection("j", attrAsElems);
        //tryWritingBean(bean, "simpleBeanWithAttributesAsElems.json", true, null, null, attrAsElems);
        tryBean(bean, "simpleBeanWithAttributesAsElems.json", true, null, null, "i j");
    }

    public void testSimpleBeanWithJustOneAttribute() throws JAXBException, IOException {
        SimpleBeanWithJustOneAttribute bean = (SimpleBeanWithJustOneAttribute)SimpleBeanWithJustOneAttribute.createTestInstance();
        tryBean(bean, "simpleBeanWithJustOneAttribute.json", true, null, null);
    }

    public void testSimpleBeanWithJustOneAttributeAsElem() throws JAXBException, IOException {
        SimpleBeanWithJustOneAttribute bean = (SimpleBeanWithJustOneAttribute)SimpleBeanWithJustOneAttribute.createTestInstance();
        Collection<String> attrAsElems = new LinkedList<String>();
        addStringsToCollection("uri", attrAsElems);
        //tryWritingBean(bean, "simpleBeanWithJustOneAttributeAsElem.json", true, null, null, attrAsElems);
        tryBean(bean, "simpleBeanWithJustOneAttributeAsElem.json", true, null, null, "uri");
    }

    public void testSimpleBeanWithJustOneAttributeAndValue() throws JAXBException, IOException {
        SimpleBeanWithJustOneAttributeAndValue bean = (SimpleBeanWithJustOneAttributeAndValue)SimpleBeanWithJustOneAttributeAndValue.createTestInstance();
        tryBean(bean, "simpleBeanWithJustOneAttributeAndValue.json", true, null, null);
    }

    public void testSimpleBeanWithJustOneAttributeAsElemAndValue() throws JAXBException, IOException {
        SimpleBeanWithJustOneAttributeAndValue bean = (SimpleBeanWithJustOneAttributeAndValue)SimpleBeanWithJustOneAttributeAndValue.createTestInstance();
        Collection<String> attrAsElems = new LinkedList<String>();
        addStringsToCollection("uri", attrAsElems);
        //tryWritingBean(bean, "simpleBeanWithJustOneAttributeAsElemAndValue.json", true, null, null, attrAsElems);
        tryBean(bean, "simpleBeanWithJustOneAttributeAsElemAndValue.json", true, null, null, "uri");
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
    
    public void testPureCharDataValue() throws JAXBException, IOException {
        PureCharDataBean bean = (PureCharDataBean)PureCharDataBean.createTestInstance();
        tryBean(bean, "pureCharDataValue.json", true, null, null);
    }

    public void testAttrAndCharDataValue() throws JAXBException, IOException {
        AttrAndCharDataBean bean = (AttrAndCharDataBean)AttrAndCharDataBean.createTestInstance();
        tryBean(bean, "attrAndCharDataValue.json", true, null, null);
    }
    
    public void testAttrAndXmlVal() throws Exception {
        SimpleBeanWithAttributes bean = (SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance();
        tryBean(bean, "simpleBeanWithAttributes.json", true, null, null);
    }

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
        tryBean(jaxbBean, filename, stripRoot, arrays, nonStrings, null);
    }

    public void tryBean(Object jaxbBean, String filename,
            boolean stripRoot, String arrays, String nonStrings, String attrAsElems) throws JAXBException, IOException {
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
            JSONConfiguration config) throws JAXBException, IOException {
        String expectedJsonExpr = ResourceHelper.getResourceAsString(PKG_NAME, expectedJsonExprFilename);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter resultWriter = new StringWriter();
        marshaller.marshal(jaxbBean, JsonXmlStreamWriter.createWriter(resultWriter, config));
        assertEquals("MISMATCH:\n" + expectedJsonExpr + "\n" + resultWriter.toString() + "\n", 
                expectedJsonExpr, resultWriter.toString());
    }

    public void tryReadingBean(String jsonExprFilename, Object expectedJaxbBean, 
            JSONConfiguration config) throws JAXBException, IOException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement jaxbElement = unmarshaller.unmarshal(
                new JsonXmlStreamReader(
                    new StringReader(ResourceHelper.getResourceAsString(PKG_NAME, jsonExprFilename)), config),
                expectedJaxbBean.getClass());
        System.out.println("unmarshalled: " + jaxbElement.getValue().toString());
        assertEquals("MISMATCH:\n" + expectedJaxbBean + "\n" + jaxbElement.getValue() + "\n", 
                expectedJaxbBean, jaxbElement.getValue());
    }
}
