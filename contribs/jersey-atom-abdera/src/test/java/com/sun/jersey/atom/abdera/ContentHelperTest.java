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

package com.sun.jersey.atom.abdera;

import com.sun.jersey.atom.abdera.impl.provider.entity.ContentBean;
import com.sun.jersey.atom.abdera.impl.provider.entity.ContentBeanProviders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.namespace.QName;
import junit.framework.TestCase;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

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
        providers = new ContentBeanProviders();
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
        ContentBean entity = new ContentBean("foo value", "bar value");
        helper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, entity);
        entity = helper.getContentEntity(entry, MediaType.APPLICATION_XML_TYPE, ContentBean.class);
        assertNotNull(entity);
        assertEquals("foo value", entity.getFoo());
        assertEquals("bar value", entity.getBar());
    }

    public void testSetContentEntity() {
        Entry entry = abdera.newEntry();
        ContentBean entity = new ContentBean("foo value", "bar value");
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

}
