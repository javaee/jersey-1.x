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

package com.sun.jersey.atom.abdera.impl.provider.entity;

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
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.namespace.QName;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.Parser;

@Provider
@Consumes("application/xml")
@Produces("application/xml")
public class ContentBeanProvider
        implements MessageBodyReader<ContentBean>,
        MessageBodyWriter<ContentBean> {

    private Abdera abdera = Abdera.getInstance();
    private static final QName BEAN_QNAME = new QName("http://example.com/schema", "bean");
    private static final QName FOO_QNAME = new QName("http://example.com/schema", "foo");
    private static final QName BAR_QNAME = new QName("http://example.com/schema", "bar");

    public boolean isReadable(Class<?> clazz, Type type,
            Annotation[] annotations,
            MediaType mediaType) {
        return clazz == ContentBean.class;
    }

    public ContentBean readFrom(Class<ContentBean> clazz,
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
        return new ContentBean(fooText, barText);
    }

    public boolean isWriteable(Class<?> clazz, Type type,
            Annotation[] annotations,
            MediaType mediaType) {
        return clazz == ContentBean.class;
    }

    public long getSize(ContentBean entity,
            Class<?> clazz, Type type,
            Annotation[] annotations,
            MediaType mediaType) {
        return -1;
    }

    public void writeTo(ContentBean entity,
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
