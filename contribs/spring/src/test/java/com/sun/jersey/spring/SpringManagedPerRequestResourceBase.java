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
package com.sun.jersey.spring;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class SpringManagedPerRequestResourceBase {
    
    public SpringManagedPerRequestResourceBase() {
    }
    
    private Item _singletonItem;
    private Item2 _prototypeItem;
    
    private int _count;
    
    protected Item getSingletonItemInstance() {
        return _singletonItem;
    }
    
    protected Item2 getPrototypeItemInstance() {
        return _prototypeItem;
    }

    @GET
    @Path( "singletonitem" )
    @ProduceMime( "application/xml" )
    public Item getSingletonItem() {
        return getSingletonItemInstance();
    }

    @PUT
    @Path( "singletonitem/value/{value}" )
    public void setSingletonItemValue( @PathParam( "value" ) String value ) {
        getSingletonItemInstance().setValue( value );
    }

    @GET
    @Path( "prototypeitem" )
    @ProduceMime( "application/xml" )
    public Item2 getPrototypeItem() {
        return getPrototypeItemInstance();
    }

    @PUT
    @Path( "prototypeitem/value/{value}" )
    public void setPrototypeItemValue( @PathParam( "value" ) String value ) {
        getPrototypeItemInstance().setValue( value );
    }
    
    @GET
    @Path( "count" )
    @ProduceMime("text/plain")
    public String getCount() {
        return String.valueOf( _count );
    }
    
    @POST
    @Path( "count" )
    public void updateCount() {
        _count++;
    }

    /**
     * @param prototypeItem the prototypeItem to set
     * @author Martin Grotzke
     */
    public void setPrototypeItem( Item2 prototypeItem ) {
        _prototypeItem = prototypeItem;
    }

    /**
     * @param singletonItem the singletonItem to set
     * @author Martin Grotzke
     */
    public void setSingletonItem( Item singletonItem ) {
        _singletonItem = singletonItem;
    }
    
}
