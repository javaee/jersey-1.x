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
package com.sun.jersey.spring25;

import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;

/**
 * This resource class is proxied by spring and intended to test, that injection
 * by jersey also works for such classes. In detail this tests
 * {@link SpringComponentProviderFactory#getInjectableInstance}.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Path("proxiedresource")
@Component
@Scope( "singleton" )
public class ProxiedResource {
    
    @Context
    private UriInfo _uriInfo;
    
    public ProxiedResource() {
    }

    @GET
    @Produces( "text/plain" )
    public String getBaseUri() {
        Path p = this.getClass().getAnnotation(Path.class);
        System.out.println("XXXXXXXX: " + p);
        System.out.println("XXXXXXXX: " + this.getClass());
        
        // if the uriInfo is not injected - of course - this produces an NPE
        return _uriInfo.getBaseUri().toString();
    }

    @Path("subresource")
    public ProxiedSubResource getSubResource(@Context ResourceContext rc) {
        return rc.getResource(ProxiedSubResource.class);
    }
}
