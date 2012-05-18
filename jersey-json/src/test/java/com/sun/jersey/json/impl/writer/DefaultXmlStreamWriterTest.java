/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.json.impl.writer;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Formatter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.json.impl.Stax2JsonFactory;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Tests for {@code DefaultXmlStreamWriterTest} abstract class.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class DefaultXmlStreamWriterTest extends TestCase {

    @XmlRootElement
    public static class ComplexXmlEventBean {

        public static Object createTestInstance() {
            ComplexXmlEventBean bean = new ComplexXmlEventBean();
            bean.setCdata("<hello>world</hello>");
            return bean;
        }

        /**
         * Note: MOXy specific annotation.
         */
        @XmlCDATA
        private String cdata;

        public String getCdata() {
            return cdata;
        }

        public void setCdata(String cdata) {
            this.cdata = cdata;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (cdata != null ? cdata.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            final ComplexXmlEventBean that = (ComplexXmlEventBean) obj;
            if (this.cdata != that.cdata && (this.cdata == null || !this.cdata.equals(that.cdata))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new Formatter().format("CXEB(cdata=%s)", cdata).toString();
        }

    }

    /**
     * Tests if XMLStreamWriter for JSON mapped notation is able to handle a MOXy specific {@code @XmlCDATA} annotation and create
     * a valid JSON which is afterwards unmarshalled to a JAXBBean and compared to the original one.
     *
     * Note: MOXy specific test.
     */
    @Test
    public void testXmlCdataAnnotationMappedNotation() throws Exception {
        _testXmlCdataAnnotation(JSONConfiguration.mapped().build());
    }

    /**
     * Tests if XMLStreamWriter for JSON natural notation is able to handle a MOXy specific {@code @XmlCDATA} annotation and
     * create a valid JSON which is afterwards unmarshalled to a JAXBBean and compared to the original one.
     *
     * Note: MOXy specific test.
     */
    @Test
    public void testXmlCdataAnnotationNaturalNotation() throws Exception {
        _testXmlCdataAnnotation(JSONConfiguration.natural().build());
    }

    /**
     * JAXB RI marshals the given bean to a JSON representation as expected even though it does not handle contents of
     * {@code ComplexXmlEventBean#cdata} field as a CData section but as a pure XML element with text data in it.
     */
    public void _testXmlCdataAnnotation(final JSONConfiguration configuration) throws Exception {
        final JAXBContext jaxbContext = new JSONJAXBContext(ComplexXmlEventBean.class);

        // Marshal
        final Marshaller marshaller = jaxbContext.createMarshaller();
        final StringWriter writer = new StringWriter();

        final Object testInstance = ComplexXmlEventBean.createTestInstance();
        marshaller.marshal(testInstance,
                Stax2JsonFactory.createWriter(writer, configuration, ComplexXmlEventBean.class, jaxbContext));

        System.out.println("Marshalled XML:");
        marshaller.marshal(testInstance, System.out);

        System.out.println("\nMarshalled JSON:");
        System.out.println(writer.toString());

        // Unmarshal
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final JAXBElement<ComplexXmlEventBean> unmarshal = unmarshaller.unmarshal(
                Stax2JsonFactory.createReader(new StringReader(writer.toString()), configuration, null,
                        ComplexXmlEventBean.class, jaxbContext, false),
                ComplexXmlEventBean.class);

        final ComplexXmlEventBean complexXmlEventBean = unmarshal.getValue();
        System.out.println("Unmarshalled bean: " + complexXmlEventBean.toString());

        assertEquals(testInstance, complexXmlEventBean);
    }

}
