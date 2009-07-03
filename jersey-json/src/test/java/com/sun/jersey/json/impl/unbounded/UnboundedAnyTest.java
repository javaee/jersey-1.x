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
package com.sun.jersey.json.impl.unbounded;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.api.json.JSONUnmarshaller;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * a test case for issue#305
 *
 * @author japod
 */
public class UnboundedAnyTest extends TestCase {

    public void testUnboundedAny() throws Exception {
        final JSONJAXBContext ctx = new JSONJAXBContext(JSONConfiguration.natural().build(), Fruit.class, Fruits.class);
        final JSONMarshaller jm = ctx.createJSONMarshaller();
        final JSONUnmarshaller ju = ctx.createJSONUnmarshaller();
        final StringWriter sw = new StringWriter();

        Fruits fruits = createFruits();

        jm.marshallToJSON(fruits, sw);

        String jsonResult = sw.toString();
        System.out.println(jsonResult);

        assertEquals("{\"fruit\":{\"name\":\"apple\",\"color\":\"red\"}}", jsonResult);
   }

    private Fruits createFruits() throws ParserConfigurationException, DOMException {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = db.newDocument();
        Fruit fruit = new Fruit();

        Element name = dom.createElement("name");
        name.setTextContent("apple");
        fruit.getAny().add(name);

        Element color = dom.createElement("color");
        color.setTextContent("red");
        fruit.getAny().add(color);

        Fruits result = new Fruits();
        result.setFruit(fruit);

        return result;
    }
}
