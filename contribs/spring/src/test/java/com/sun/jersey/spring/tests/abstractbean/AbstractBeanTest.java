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
package com.sun.jersey.spring.tests.abstractbean;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spring.tests.AbstractTest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.springframework.stereotype.Component;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Test
public class AbstractBeanTest extends AbstractTest {

    public static abstract class XMLDeclaredAbstractResource {
    }

    @Path("xml")
    public static class XMLDeclaredResource extends XMLDeclaredAbstractResource {

        @GET
        public String get() {
            return "xml";
        }
    }

    @Test
    public void testXmlDeclared() {
        start();

        WebResource r = resource("xml");
        Assert.assertEquals("xml", r.get(String.class));
    }


    @Component
    public static abstract class AutoDeclaredAbstractResource {
    }

    @Path("auto")
    @Component
    public static class AutoDeclaredResource extends AutoDeclaredAbstractResource {

        @GET
        public String get() {
            return "auto";
        }
    }

    @Test
    public void testAutolDeclared() {
        start();

        WebResource r = resource("auto");
        Assert.assertEquals("auto", r.get(String.class));
    }
}
