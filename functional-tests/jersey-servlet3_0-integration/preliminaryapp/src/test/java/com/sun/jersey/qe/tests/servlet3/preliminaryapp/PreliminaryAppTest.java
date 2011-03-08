/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.qe.tests.servlet3.preliminaryapp;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naresh
 */
public class PreliminaryAppTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new ExternalTestContainerFactory();
    }

    public PreliminaryAppTest() {
        super(new WebAppDescriptor.Builder()
                .build());
    }

    @Test
    public void testOneApplication() {
        WebResource webResource = resource();
        String response = webResource.path("preliminaryapp").path("oneonly").path("one").get(String.class);
        assertEquals("Expected response not seen.", "ONE", response);
    }

    @Test
    public void testTwoApplication() {
        WebResource webResource = resource();
        String response = webResource.path("preliminaryapp").path("twoonly").path("two").get(String.class);
        assertEquals("Expected response not seen.", "TWO", response);
    }

    @Test
    public void testOneAndTwoApplication() {
        WebResource webResource = resource();
        String response = webResource.path("preliminaryapp").path("oneandtwo").path("one").get(String.class);
        assertEquals("Expected response not seen.", "ONE", response);
        response = webResource.path("preliminaryapp").path("oneandtwo").path("two").get(String.class);
        assertEquals("Expected response not seen.", "TWO", response);
    }

    @Test
    public void testOneApplication404() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("preliminaryapp").path("oneonly")
                .path("two").get(ClientResponse.class);
        assertEquals("Expected response not seen.", 404, response.getStatus());
    }

    @Test
    public void testTwoApplication404() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("preliminaryapp").path("twoonly")
                .path("one").get(ClientResponse.class);
        assertEquals("Expected response not seen.", 404, response.getStatus());
    }
}
