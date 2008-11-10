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
package com.sun.jersey.spring.jerseymanaged;

import com.sun.jersey.spring.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;

/**
 * A resource class that injects two spring beans of the same type using the
 * @Inject.value to differentiate between the beans.<br>
 * Created on: Jun 19., 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Path( NameBasedInjectingResource.PATH )
@Singleton
public class NameBasedInjectingResource {
    
    static final String PATH = "NameBasedInjectingResource";
    
    @Inject( "namedItem3_1" )
    private Item3 _item1;
    @Inject( "namedItem3_2" )
    private Item3 _item2;
    
    public NameBasedInjectingResource() {
    }

    @GET
    @Path( "{value}" )
    @Produces( "application/xml" )
    public Item3 getItemByValue( @PathParam( "value" ) String value ) {
        if ( value.equals( _item1.getValue() ) ) {
            return _item1;
        }
        else if ( value.equals( _item2.getValue() ) ) {
            return _item2;
        }
        else {
            throw new NotFoundException( "No item with value '" + value + "' found." );
        }
    }
    
}
