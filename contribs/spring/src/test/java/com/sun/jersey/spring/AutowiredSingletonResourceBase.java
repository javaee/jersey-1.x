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
import javax.ws.rs.Produces;

/**
 * A singleton resource class that is not managed by spring (but jersey)<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AutowiredSingletonResourceBase {
    
    private Item _item;
    private int _countUsage;
    
    public AutowiredSingletonResourceBase() {
    }

    @GET
    @Path( "item" )
    @Produces( "application/xml" )
    public Item getItem() {
        return _item;
    }

    /**
     * @param item the item to set
     * @author Martin Grotzke
     */
    public void setItem( Item item ) {
        _item = item;
    }

    @PUT
    @Path( "item/value/{value}" )
    public void setItemValue( @PathParam( "value" ) String value ) {
        _item.setValue( value );
    }
    
    @GET
    @Path( "countusage" )
    @Produces("text/plain")
    public String getCountUsage() {
        return String.valueOf( _countUsage );
    }
    
    @POST
    @Path( "countusage" )
    public void updateCountUsage() {
        _countUsage++;
    }
    
}
