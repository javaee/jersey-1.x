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
package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.provider.EntityHolder;
import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityHolderTest extends AbstractResourceTester {
    public EntityHolderTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    public static class EntityHolderResource {
        @Path("string")
        @POST
        public String post(EntityHolder<String> s) {
            if (s.hasEntity()) {
                return s.getEntity();
            } else {
                return "EMPTY";
            }
        }

        @Path("jaxb")
        @Consumes("application/xml")
        @Produces("application/xml")
        @POST
        public JAXBBean post2(EntityHolder<JAXBBean> s) {
            if (s.hasEntity()) {
                return s.getEntity();
            } else {
                return new JAXBBean("EMPTY");
            }
        }

    }


    public void testString() {
        initiateWebApplication(EntityHolderResource.class);

        WebResource r = resource("/");

        String s = r.path("string").post(String.class);
        assertEquals("EMPTY", s);

        s = r.path("string").type("text/plain").post(String.class);
        assertEquals("EMPTY", s);

        s = r.path("string").post(String.class, "CONTENT");
        assertEquals("CONTENT", s);
    }

    public void testJAXB() {
        initiateWebApplication(EntityHolderResource.class);

        WebResource r = resource("/");

        JAXBBean b = r.path("jaxb").post(JAXBBean.class);
        assertEquals("EMPTY", b.value);

        b = r.path("jaxb").type("application/xml").post(JAXBBean.class);
        assertEquals("EMPTY", b.value);

        b = r.path("jaxb").post(JAXBBean.class, new JAXBBean("CONTENT"));
        assertEquals("CONTENT", b.value);
    }
}