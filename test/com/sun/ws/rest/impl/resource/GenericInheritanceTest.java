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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class GenericInheritanceTest extends AbstractResourceTester {
    
    public GenericInheritanceTest(String testName) {
        super(testName);
    }

    static public abstract class GenericSuperResource<T> {
        @PUT
        public void update(T t) {
            System.out.println(t.toString());
        }
    }
    
    @Path("/")
    static public class GenericSubResource extends GenericSuperResource<String> {
        @POST
        public void create(String t) {
            System.out.println(t.toString());
        }    
    }
    
    public void testGenericSuperResourcePut() {
        initiateWebApplication(GenericSubResource.class);
        resourceProxy("/").put("test");
    }    
}