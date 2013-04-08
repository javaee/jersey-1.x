/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.samples.jersey_ejb.test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author pavel.bucek@sun.com
 */
public class MessageBoardTest extends JerseyTest {

    public MessageBoardTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.samples.jersey_ejb.resources")
                .contextPath("jersey-ejb")
                .build());
    }

    @Test public void testDeployed() {
        WebResource webResource = resource();
        String s = webResource.get(String.class);
        assertFalse(s.length() == 0);
    }

    @Test public void testAddMessage() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("app/messages").post(ClientResponse.class, "hello world!");

        assertTrue("Response status should be CREATED. Current value is \"" + response.getClientResponseStatus() + "\"",
                response.getClientResponseStatus() == ClientResponse.Status.CREATED);



        client().resource(response.getLocation()).delete(); // remove added message
    }

    @Test public void testDeleteMessage() {
        WebResource webResource = resource();
        URI u = webResource.getURI(); // just placeholder

        ClientResponse response = webResource.path("app/messages").post(ClientResponse.class, "toDelete");
        if(response.getClientResponseStatus() == ClientResponse.Status.CREATED) {
            u = response.getLocation();
        } else {
            assertTrue(false);
        }

        String s = client().resource(u).get(String.class);

        assertTrue(s.contains("toDelete"));

        client().resource(u).delete();

        boolean caught = false;

        try {
            s = client().resource(u).get(String.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                caught = true;
            }
        }
        assertTrue(caught);

        caught = false;

        try {
            client().resource(u).delete();
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                caught = true;
            }
        }
        assertTrue(caught);
    }
}

