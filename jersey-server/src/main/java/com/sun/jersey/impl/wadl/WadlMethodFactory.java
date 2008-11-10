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

package com.sun.jersey.impl.wadl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.jersey.impl.model.method.ResourceMethod;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Resource;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
/* package */ final class WadlMethodFactory {

    public static final class WadlOptionsMethod extends ResourceMethod {
        public WadlOptionsMethod(Map<String, List<ResourceMethod>> methods, 
                AbstractResource resource, String path, WadlGenerator wadlGenerator) {
            super("OPTIONS",
                    UriTemplate.EMPTY,
                    MediaTypes.GENERAL_MEDIA_TYPE_LIST, 
                    MediaTypes.GENERAL_MEDIA_TYPE_LIST,
                    false,
                    new WadlOptionsMethodDispatcher(methods, resource, path, wadlGenerator));        
        }

        @Override
        public String toString() {
            return "WADL OPTIONS method" ;
        }
    }
    
    private static final class WadlOptionsMethodDispatcher extends 
            ResourceHttpOptionsMethod.OptionsRequestDispatcher {
        private final AbstractResource resource;
        private final String path;
        private final WadlGenerator wadlGenerator;
        
        WadlOptionsMethodDispatcher(Map<String, List<ResourceMethod>> methods,
                AbstractResource resource, String path, WadlGenerator wadlGenerator) {
            super(methods);
            this.resource = resource;
            this.path = path;
            this.wadlGenerator = wadlGenerator;
        }
            
        @Override
        public void dispatch(final Object o, final HttpContext context) {            
            final Application a = generateApplication(context.getUriInfo(), 
                    resource, path, wadlGenerator);
            
            context.getResponse().setResponse(
                    Response.ok(a, MediaTypes.WADL).header("Allow", allow).build());
        }
    }
    
    private static Application generateApplication(UriInfo info, 
            AbstractResource resource, String path, WadlGenerator wadlGenerator) {   
        Application a = path == null ? new WadlBuilder( wadlGenerator ).generate(resource) : 
            new WadlBuilder( wadlGenerator ).generate(resource, path);
        
        a.getResources().setBase(info.getBaseUri().toString());
                
        final Resource r = a.getResources().getResource().get(0);
        r.setPath(info.getBaseUri().relativize(
                info.getAbsolutePath()).toString());
        
        // remove path params since path is fixed at this point
        r.getParam().clear();
        
        return a;
    }
}
