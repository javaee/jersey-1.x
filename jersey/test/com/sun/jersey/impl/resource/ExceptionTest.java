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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ExceptionTest extends AbstractResourceTester {
    
    public ExceptionTest(String testName) {
        super(testName);
    }

    static public class CheckedException extends Exception {
        public CheckedException() {
            super();
        }
    }
    
    @Path("/exception/checked")
    static public class ExceptionCheckedResource { 
        @GET
        public String get() throws CheckedException {
            throw new CheckedException();
        }
    }

    public void testExceptionChecked() {
        initiateWebApplication(ExceptionCheckedResource.class);
        
        boolean caught = false;
        try {
            resource("/exception/checked").get(ClientResponse.class);
        } catch (ContainerException e) {
            caught = true;
            assertEquals(CheckedException.class, e.getCause().getClass());
        }
    }
    
    @Path("/exception/runtime")
    static public class ExceptionRutimeResource { 
        @GET
        public String get() {
            throw new UnsupportedOperationException();
        }
    }
    
    public void testExceptionRuntime() {
        initiateWebApplication(ExceptionRutimeResource.class);
        
        boolean caught = false;
        try {
            resource("/exception/runtime").get(ClientResponse.class);
        } catch (UnsupportedOperationException e) {
            caught = true;
        }
        assertEquals(true, caught);
    }
    
    @Path("/exception/webapplication/{status}")
    static public class ExceptionWebApplicationResource { 
        @GET
        public String get(@PathParam("status") int status) {
            throw new WebApplicationException(status);
        }
    }
    
    public void test400StatusCode() {
        initiateWebApplication(ExceptionWebApplicationResource.class);

        ClientResponse cr = resource("/exception/webapplication/400", false).
                get(ClientResponse.class);        
        assertEquals(400, cr.getStatus());
    }
    
    public void test500StatusCode() {
        initiateWebApplication(ExceptionWebApplicationResource.class);

        ClientResponse cr = resource("/exception/webapplication/500", false).
                get(ClientResponse.class);        
        assertEquals(500, cr.getStatus());
    }   
}