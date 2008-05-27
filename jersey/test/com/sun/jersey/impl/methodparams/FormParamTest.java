/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */
package com.sun.jersey.impl.methodparams;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.representation.FormParam;
import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FormParamTest extends AbstractResourceTester {

    public FormParamTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    public class Resource {
        @POST
        @ConsumeMime(MediaType.APPLICATION_FORM_URLENCODED)
        public String post(@FormParam("a") String a, @FormParam("b") String b) {
            return a + b;
        }
    }
    
    public void testFormParam() {
        initiateWebApplication(Resource.class);
        
        WebResource r = resource("/");
        
        Form form = new Form();
        form.add("a", "foo");
        form.add("b", "bar");        
        
        String s = r.post(String.class, form);
        assertEquals("foobar", s);
    }    
}