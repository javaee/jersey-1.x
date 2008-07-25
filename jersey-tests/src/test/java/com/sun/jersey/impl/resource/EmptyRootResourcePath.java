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
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyRootResourcePath extends AbstractResourceTester {
    
    public EmptyRootResourcePath(String testName) {
        super(testName);
    }

    @Path("")
    public static class VirtualResource {
        @Context UriInfo uriInfo;
        
        public @Path("one") Object getOne() {
            return new SubResource(uriInfo.getPath());
        }
        
        public @Path("two") Object getTwo() {
            return new SubResource(uriInfo.getPath());            
        }
    }
    
    public static class SubResource {
        private String path;
        
        SubResource(String path) {
            this.path = path;
        }
        
        public @GET String get() {
            return path;
        }
    }
    
    @Path("absolute")
    public static class AbsoluteResource {
        @Context UriInfo uriInfo;
        
        public @GET String get() {
            return uriInfo.getPath();
        }
    }
    
    public void testGet() throws IOException {
        initiateWebApplication(VirtualResource.class, AbsoluteResource.class);

        WebResource r = resource("/one");
        assertEquals("one", r.get(String.class));
        
        r = resource("/two");
        assertEquals("two", r.get(String.class));
        
        r = resource("/absolute");
        assertEquals("absolute", r.get(String.class));
    }
}