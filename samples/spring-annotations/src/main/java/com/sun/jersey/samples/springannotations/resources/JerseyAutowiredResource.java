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

package com.sun.jersey.samples.springannotations.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.samples.springannotations.model.Item;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This resource demonstrates how users of spring-2.0 can use annotations based
 * autowiring of their beans, even if spring-2.0 does not provide this.<br/>
 * This shows the usage of the {@link Autowire} annotations provided by jersey-spring.
 * <p>
 * The referenced {@link Item} bean here is loaded by spring automatically (using the
 * component-scan), if you use spring-2.0 of course you had to define this bean in the
 * applicationContext.xml.
 * </p>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Path("/jersey-autowired")
@Singleton
@Autowire
public class JerseyAutowiredResource {
    
    private Item _item;
    
    @GET
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
    
}