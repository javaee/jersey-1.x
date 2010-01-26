/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.api.json.JSONUnmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import junit.framework.TestCase;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class NamespaceElementTest extends TestCase {

    final boolean jsonEnabled = true;
    final NamespaceBean one = TestHelper.createTestInstance(NamespaceBean.class);

    public void _disabledFailingtestBadgerfish() throws Exception {
        tryConfiguration(JSONConfiguration.badgerFish().build());
    }

    public void testMappedJettison() throws Exception {
        Map<String, String> ns2json = new HashMap<String, String>();
        ns2json.put("http://example.com", "example");
        tryConfiguration(JSONConfiguration.mappedJettison().xml2JsonNs(ns2json).build());
    }

    public void testNatural() throws Exception {
        tryConfiguration(JSONConfiguration.natural().build());
    }

    public void testMapped() throws Exception {
        Map<String, String> ns2json = new HashMap<String, String>();
        ns2json.put("http://example.com", "example");
        tryConfiguration(JSONConfiguration.mapped().xml2JsonNs(ns2json).rootUnwrapping(false).build());
    }

    private void tryConfiguration(JSONConfiguration configuration) throws Exception {

        final JSONJAXBContext ctx = new JSONJAXBContext(configuration, NamespaceBean.class);
        final JSONMarshaller jm = ctx.createJSONMarshaller();
        final JSONUnmarshaller ju = ctx.createJSONUnmarshaller();


        NamespaceBean beanTwo;

        final StringWriter sw = new StringWriter();

        jm.marshallToJSON(one, sw);

        System.out.println(String.format("%s", sw));

        beanTwo = ju.unmarshalFromJSON(new StringReader(sw.toString()), NamespaceBean.class);

        assertEquals(one, beanTwo);
    }
}
