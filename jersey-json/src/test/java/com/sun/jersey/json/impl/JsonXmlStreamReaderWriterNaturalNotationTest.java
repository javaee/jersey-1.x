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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
import com.sun.jersey.json.impl.writer.Stax2JacksonWriter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import static com.sun.jersey.json.impl.util.JSONNormalizer.normalizeJsonString;

import junit.framework.TestCase;

/**
 * {@code JsonXmlStreamReader} roundtrip tests for JSON in the natural notation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class JsonXmlStreamReaderWriterNaturalNotationTest extends TestCase {

    private static final String PKG_NAME = "com/sun/jersey/json/impl/";

    public JsonXmlStreamReaderWriterNaturalNotationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAttrAndCharData() throws Exception {
        tryBean(AttrAndCharDataBean.class, "attrAndCharDataValue_natural.json", AttrAndCharDataBean.createTestInstance());
    }

    public void testComplexBeanWithAttributes() throws Exception {
        tryBean(ComplexBeanWithAttributes.class, "complexBeanWithAttributes_natural.json",
                ComplexBeanWithAttributes.createTestInstance());
    }

    public void testComplexBeanWithAttributes2() throws Exception {
        tryBean(ComplexBeanWithAttributes2.class, "complexBeanWithAttributes2_natural.json",
                ComplexBeanWithAttributes2.createTestInstance());
    }

    public void testComplexBeanWithAttributes3() throws Exception {
        tryBean(ComplexBeanWithAttributes3.class, "complexBeanWithAttributes3_natural.json",
                ComplexBeanWithAttributes3.createTestInstance());
    }

    public void testComplexBeanWithAttributes4() throws Exception {
        tryBean(ComplexBeanWithAttributes4.class, "complexBeanWithAttributes4_natural.json",
                ComplexBeanWithAttributes4.createTestInstance());
    }

    public void testEncodedContentBean() throws Exception {
        tryBean(EncodedContentBean.class, "encodedContentBean_natural.json", EncodedContentBean.createTestInstance());
    }

    public void testListAndNonListBean() throws Exception {
        tryBean(ListAndNonListBean.class, "listAndNonList_natural.json", ListAndNonListBean.createTestInstance());
    }

    public void testListEmptyBean() throws Exception {
        tryBean(ListEmptyBean.class, "listEmptyBean_natural.json", ListEmptyBean.createTestInstance());
    }

    public void testListWrapperBean() throws Exception {
        tryBean(ListWrapperBean.class, "listWrapperBean_natural.json", ListWrapperBean.createTestInstance());
    }

    public void testPureCharDataBean() throws Exception {
        tryBean(PureCharDataBean.class, "pureCharDataBean_natural.json", PureCharDataBean.createTestInstance());
    }

    public void testSimpleBean() throws Exception {
        tryBean(SimpleBean.class, "simpleBean_natural.json", SimpleBean.createTestInstance());
    }

    public void testSimpleEmptyBean() throws Exception {
        tryBean(SimpleBean.class, "simpleBeanEmpty_natural.json", new SimpleBean());
    }

    public void testSimpleBeanWithAttributes() throws Exception {
        tryBean(SimpleBeanWithAttributes.class, "simpleBeanWithAttributes_natural.json",
                SimpleBeanWithAttributes.createTestInstance());
    }

    public void testSimpleBeanWithObjectAttributes() throws Exception {
        tryBean(SimpleBeanWithObjectAttributes.class, "simpleBeanWithObjectAttributes_natural.json",
                SimpleBeanWithObjectAttributes.createTestInstance());
    }

    public void testSimpleBeanWithJustOneAttribute() throws Exception {
        tryBean(SimpleBeanWithJustOneAttribute.class, "simpleBeanWithJustOneAttribute_natural.json",
                SimpleBeanWithJustOneAttribute.createTestInstance());
    }

    public void testSimpleBeanWithJustOneAttributeAndValue() throws Exception {
        tryBean(SimpleBeanWithJustOneAttributeAndValue.class, "simpleBeanWithJustOneAttributeAndValue_natural.json",
                SimpleBeanWithJustOneAttributeAndValue.createTestInstance());
    }

    public void testTreeModelBean() throws Exception {
        tryBean(TreeModel.class, "treeModel_natural.json", TreeModel.createTestInstance());
    }

    public void testTreeModelRootWithOneChild() throws Exception {
        tryBean(TreeModel.class, "treeModelRootWithOneChild_natural.json", TreeModel.createTestInstanceWithRootAndOneChildNode());
    }

    public void testTreeModelRootWithMultipleChildren() throws Exception {
        tryBean(TreeModel.class, "treeModelRootWithMultipleChildren_natural.json", TreeModel.createTestInstanceWithRootAndMultipleChildNodes());
    }

    public void testTwoListsWrapperBean() throws Exception {
        tryBean(TwoListsWrapperBean.class, "twoListsWrapperBean_natural.json", TwoListsWrapperBean.createTestInstance());
    }

    public void testUser() throws Exception {
        tryBean(User.class, "user_natural.json", User.createTestInstance());
    }

    public void testUserTable() throws Exception {
        tryBean(UserTable.class, "userTable_natural.json", UserTable.createTestInstance());
    }

    public void testJersey1199() throws Exception {
        Map<String, Object> props = JSONHelper.createPropertiesForJaxbContext(Collections.<String, Object>emptyMap());
        Class[] classes = new Class[] {ColorHolder.class, Jersey1199List.class};

        final JSONConfiguration.NaturalBuilder builder = JSONConfiguration.natural();
        builder.usePrefixesAtNaturalAttributes();
        builder.rootUnwrapping(false);

        final JSONConfiguration jsonConfiguration = builder.build();
        final JAXBContext jaxbContext = JAXBContext.newInstance(classes, props);

        tryBean(Jersey1199List.class, "jersey1199_natural.json", Jersey1199List.createTestInstance(), jaxbContext, jsonConfiguration);
    }

    public void testListEmptyBeanVerbose() throws Exception {
        Map<String, Object> props = JSONHelper.createPropertiesForJaxbContext(Collections.<String, Object>emptyMap());
        Class[] classes = new Class[] {ListEmptyBean.class};

        JAXBContext jaxbContext = JAXBContext.newInstance(classes, props);

        tryReadingBean(ListEmptyBean.class, "listEmptyBeanVerbose_natural.json", ListEmptyBean.createTestInstance(), jaxbContext, null);
    }

    private void tryBean(final Class clazz,
                         final String jsonExprFilename,
                         final Object jaxbBean) throws Exception {
        Map<String, Object> props = JSONHelper.createPropertiesForJaxbContext(Collections.<String, Object>emptyMap());
        Class[] classes = new Class[]{clazz};

        JAXBContext jaxbContext = JAXBContext.newInstance(classes, props);

        tryBean(clazz, jsonExprFilename, jaxbBean, jaxbContext, null);
    }

    private void tryBean(final Class clazz,
                         final String jsonExprFilename,
                         final Object jaxbBean,
                         final JAXBContext jaxbContext,
                         final JSONConfiguration configuration) throws Exception {
        tryWritingBean(clazz, jsonExprFilename, jaxbBean, jaxbContext, configuration);
        tryReadingBean(clazz, jsonExprFilename, jaxbBean, jaxbContext, configuration);
    }

    private void tryWritingBean(final Class clazz,
                                final String jsonExprFilename,
                                final Object jaxbBean,
                                final JAXBContext jaxbContext,
                                final JSONConfiguration configuration) throws Exception {
        String expectedJsonExpr = JSONTestHelper.getResourceAsString(PKG_NAME, jsonExprFilename);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter resultWriter = new StringWriter();

        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(resultWriter);

        final Stax2JacksonWriter writer = configuration != null
                ? new Stax2JacksonWriter(jsonGenerator, configuration, clazz, jaxbContext) : new Stax2JacksonWriter(jsonGenerator, clazz, jaxbContext);

        marshaller.marshal(jaxbBean, writer);
        System.out.println(String.format("Marshalled: %s", resultWriter.toString()));
        assertEquals("MISMATCH:\n" + expectedJsonExpr + "\n" + resultWriter.toString() + "\n",
                normalizeJsonString(expectedJsonExpr), normalizeJsonString(resultWriter.toString()));
    }

    private void tryReadingBean(final Class clazz,
                                final String jsonExprFilename,
                                final Object jaxbBean,
                                final JAXBContext jaxbContext,
                                JSONConfiguration configuration) throws Exception {

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        if (configuration == null) {
            configuration = JSONConfiguration.natural().rootUnwrapping(false).build();
        }

        final XMLStreamReader xmlStreamReader = JsonXmlStreamReader.create(
                new StringReader(JSONTestHelper.getResourceAsString(PKG_NAME, jsonExprFilename)),
                configuration,
                null, clazz, jaxbContext, false);

        Object unmarshalledBean = unmarshaller.unmarshal(xmlStreamReader, clazz).getValue();

        System.out.println(String.format("Unmarshalled: %s", unmarshalledBean));
        assertEquals("MISMATCH:\n" + jaxbBean + "\n" + unmarshalledBean + "\n",
                jaxbBean, unmarshalledBean);
    }

}
