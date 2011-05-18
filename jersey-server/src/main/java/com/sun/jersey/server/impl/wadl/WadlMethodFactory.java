/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.server.impl.wadl;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.server.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.jersey.server.impl.model.method.ResourceMethod;
import com.sun.jersey.server.wadl.WadlApplicationContext;
import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
/* package */ final class WadlMethodFactory {

    public static final class WadlOptionsMethod extends ResourceMethod {
        public WadlOptionsMethod(Map<String, List<ResourceMethod>> methods,
                                 AbstractResource resource, String path, WadlGenerator wadlGenerator,
                                 WadlApplicationContext wadlApplicationContext) {
            super("OPTIONS",
                    UriTemplate.EMPTY,
                    MediaTypes.GENERAL_MEDIA_TYPE_LIST,
                    MediaTypes.GENERAL_MEDIA_TYPE_LIST,
                    false,
                    new WadlOptionsMethodDispatcher(methods, resource, path, wadlGenerator, wadlApplicationContext));
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
        private final WadlApplicationContext wadlApplicationContext;

        WadlOptionsMethodDispatcher(Map<String, List<ResourceMethod>> methods,
                                    AbstractResource resource, String path, WadlGenerator wadlGenerator,
                                    WadlApplicationContext wadlApplicationContext) {
            super(methods);
            this.resource = resource;
            this.path = path;
            this.wadlGenerator = wadlGenerator;
            this.wadlApplicationContext = wadlApplicationContext;
        }

        @Override
        public void dispatch(final Object o, final HttpContext context) {
            if(wadlApplicationContext.isWadlGenerationEnabled()) {
                final Application a = generateApplication(context.getUriInfo(),
                        resource, path, wadlGenerator);

                context.getResponse().setResponse(
                        Response.ok(a, MediaTypes.WADL).header("Allow", allow).build());
            } else {
                if(!wadlApplicationContext.isWadlGenerationEnabled())
                    context.getResponse().setResponse(Response.status(Response.Status.NO_CONTENT).header("Allow", allow).build());
            }
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
