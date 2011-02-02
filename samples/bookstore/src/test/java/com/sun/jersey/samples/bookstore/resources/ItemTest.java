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
package com.sun.jersey.samples.bookstore.resources;

import com.sun.jersey.api.client.WebResource;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @version $Revision: 1.1 $
 */
public class ItemTest extends TestSupport {

    public ItemTest()  throws Exception {

    }

    @Test
    public void testResourceAsHtml() throws Exception {
        String response = item1resource().get(String.class);
        assertItemHtmlResponse(response);
    }

    @Test
    public void testResourceAsXml() throws Exception {
        String text = item1resource().accept("application/xml").get(String.class);
        System.out.println("Item XML is: " + text);
        
        Book response = item1resource().accept("application/xml").get(Book.class);
        assertNotNull("Should have returned an item!", response);
        assertEquals("item title", "Svejk", response.getTitle());
    }

    @Test
    public void testResourceAsHtmlUsingFirefoxAcceptHeaders() throws Exception {
        String response = item1resource().accept(
                "text/html",
                "application/xhtml+xml",
                "application/xml;q=0.9",
                "*/*;q=0.8").get(String.class);
        assertItemHtmlResponse(response);
    }

    @Test
    public void testResourceAsHtmlUsingSafariAcceptHeaders() throws Exception {
        WebResource.Builder resource = item1resource().accept(
                "text/xml",
                "application/xml",
                "application/xhtml+xml",
                "text/html;q=0.9",
                "text/plain;q=0.8,image/png",
                "*/*;q=0.5");
        String response = resource.get(String.class);
        assertItemHtmlResponse(response);
    }

    protected void assertItemHtmlResponse(String response) {
        assertHtmlResponse(response);
        assertResponseContains(response, "<title>Book</title>");
        assertResponseContains(response, "<h1>Svejk</h1>");
    }

    protected WebResource item1resource() {
        return resource().path("/items/1");
    }


}
