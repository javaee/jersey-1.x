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

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.PerRequest;
import com.sun.jersey.spring.Item;

/**
 * This prerequest resource only uses spring annotations (there's no definition in applicationContext.xml)
 * and refers other beans using the JSR-250 annotation {@link Resource}.<br/>
 * The spring scope has to be defined, as otherwise spring would create this bean with scope singleton.<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Path("annotatedPerRequest")
@PerRequest
@Component
@Scope( "prototype" )
public class AnnotatedPerRequestResource {
    
    @Resource
    private Item _definedSingletonItem;
    
    @Resource
    private SpringComponent _annotatedSpringComponent;
    
    public AnnotatedPerRequestResource() {
    }

    @GET
    @Path( "definedsingletonitem" )
    @Produces( "application/xml" )
    public Item getSingletonItem() {
        return _definedSingletonItem;
    }

    @GET
    @Path( "annotatedspringcomponent" )
    @Produces( "application/xml" )
    public SpringComponent getAnnotatedSpringComponent() {
        return _annotatedSpringComponent;
    }
    
}
