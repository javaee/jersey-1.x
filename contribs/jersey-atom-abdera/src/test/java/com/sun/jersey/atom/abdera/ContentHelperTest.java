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

package com.sun.jersey.atom.abdera;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.namespace.QName;
import junit.framework.TestCase;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;

/**
 * <p>Unit tests for {@link ContentHelper}.</p>
 */
public class ContentHelperTest extends TestCase {

    public ContentHelperTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        providers = new ContentHelperProviders();
        helper = new ContentHelper(providers);
    }

    @Override
    protected void tearDown() throws Exception {
        helper = null;
        providers = null;
        super.tearDown();
    }

    private Abdera abdera = Abdera.getInstance();
    private ContentHelper helper = null;
    private Providers providers = null;

    private static final QName BEAN_QNAME = new QName("http://example.com/schema", "bean");
    private static final QName FOO_QNAME =  new QName("http://example.com/schema", "foo");
    private static final QName BAR_QNAME =  new QName("http://example.com/schema", "bar");

    public void testGetContentEntity() {
        Entry entry = abdera.newEntry();
        ContentHelperBean entity = new ContentHelperBean("foo value", "bar value");
        helper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, entity);
        entity = helper.getContentEntity(entry, MediaType.APPLICATION_XML_TYPE, ContentHelperBean.class);
        assertNotNull(entity);
        assertEquals("foo value", entity.getFoo());
        assertEquals("bar value", entity.getBar());
    }

    public void testSetContentEntity() {
        Entry entry = abdera.newEntry();
        ContentHelperBean entity = new ContentHelperBean("foo value", "bar value");
        helper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, entity);
        Content content = entry.getContentElement();
        assertEquals("application/xml", content.getMimeType().toString());
        Element bean = content.getValueElement();
        System.out.println("VAL=" + bean.toString());
        assertEquals(BEAN_QNAME, bean.getQName());
        Element foo = bean.getFirstChild(FOO_QNAME);
        assertNotNull(foo);
        assertEquals("foo value", foo.getText());
        Element bar = bean.getFirstChild(BAR_QNAME);
        assertNotNull(bar);
        assertEquals("bar value", bar.getText());
    }

    class ContentHelperBean {

        public ContentHelperBean() {}

        public ContentHelperBean(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }

        private String foo = null;
        private String bar = null;

        public String getBar() { return this.bar; }
        public void setBar(String bar) { this.bar = bar; }
        public String getFoo() { return this.foo; }
        public void setFoo(String foo) { this.foo = foo; }

    }

    @Provider
    @Consumes("application/xml")
    @Produces("application/xml")
    class ContentHelperProvider
            implements MessageBodyReader<ContentHelperBean>,
                       MessageBodyWriter<ContentHelperBean> {

        private Abdera abdera = Abdera.getInstance();

        public boolean isReadable(Class<?> clazz, Type type,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return clazz == ContentHelperBean.class;
        }

        public ContentHelperBean readFrom(Class<ContentHelperBean> clazz,
                                          Type type,
                                          Annotation[] annotations,
                                          MediaType mediaType,
                                          MultivaluedMap<String, String> headers,
                                          InputStream stream) throws IOException, WebApplicationException {
            Parser parser = abdera.getParser();
            Document<Element> document = parser.parse(stream);
            Element root = document.getRoot();
            Element foo = root.getFirstChild(FOO_QNAME);
            String fooText = null;
            if (foo != null) {
                fooText = foo.getText();
            }
            Element bar = root.getFirstChild(BAR_QNAME);
            String barText = null;
            if (bar != null) {
                barText = bar.getText();
            }
            return new ContentHelperBean(fooText, barText);
        }

        public boolean isWriteable(Class<?> clazz, Type type,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            return clazz == ContentHelperBean.class;
        }

        public long getSize(ContentHelperBean entity,
                            Class<?> clazz, Type type,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return -1;
        }

        public void writeTo(ContentHelperBean entity,
                            Class<?> clazz, Type type,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> headers,
                            OutputStream stream) throws IOException, WebApplicationException {
            Element root = abdera.getFactory().newElement(BEAN_QNAME);
            if (entity.getFoo() != null) {
                Element foo = abdera.getFactory().newElement(FOO_QNAME, root);
                foo.setText(entity.getFoo());
            }
            if (entity.getBar() != null) {
                Element bar = abdera.getFactory().newElement(BAR_QNAME, root);
                bar.setText(entity.getBar());
            }
            root.writeTo(stream);
        }
        
    }

    class ContentHelperProviders implements Providers {

        ContentHelperProvider provider = new ContentHelperProvider();

        public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> clazz,
                Type type, Annotation[] annotations, MediaType mediaType) {
            if ((clazz == ContentHelperBean.class) &&
                mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
                return (MessageBodyReader<T>) provider;
            } else {
                return null;
            }
        }

        public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> clazz,
                Type type, Annotation[] annotations, MediaType mediaType) {
            if ((clazz == ContentHelperBean.class) &&
                mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
                return (MessageBodyWriter<T>) provider;
            } else {
                return null;
            }
        }

        public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> clazz) {
            return null;
        }

        public <T> ContextResolver<T> getContextResolver(Class<T> clazz,
                MediaType type) {
            return null;
        }
        
    }

}
