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

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TerminatingSlashTest extends AbstractResourceTester {
    
    public TerminatingSlashTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class TerminatingSlash {
        @Path("abc")
        @GET
        public String get(@Context UriInfo ui) {
            return ui.getPath();
        }
    }
    
    public void testNoSlash() throws IOException {
        initiateWebApplication(TerminatingSlash.class);
        WebResource r = resource("/");
        
        String s = r.path("abc").get(String.class);
        assertEquals("abc", s);
    }

    public void testSlash() throws IOException {
        initiateWebApplication(TerminatingSlash.class);
        WebResource r = resource("/");
        
        String s = r.path("abc/").get(String.class);
        assertEquals("abc/", s);
    }   
    
    
    @Path("/")
    public class TerminatingSlashWithParameter {
        @GET
        @Path("/abc/")
        public String get() {
            return "abc";
        }

        @GET
        @Path("/abc/{id}/")
        public String getId(@PathParam("id") long id) {
            return "abc/" + id;
        }
    }

    public void testX() throws IOException {
        initiateWebApplication(TerminatingSlashWithParameter.class);
        WebResource r = resource("/");
        
        String s = r.path("abc").get(String.class);
        assertEquals("abc", s);
        s = r.path("abc/").get(String.class);
        assertEquals("abc", s);
        s = r.path("abc/1").get(String.class);
        assertEquals("abc/1", s);
        s = r.path("abc/1/").get(String.class);
        assertEquals("abc/1", s);                
    }   
}