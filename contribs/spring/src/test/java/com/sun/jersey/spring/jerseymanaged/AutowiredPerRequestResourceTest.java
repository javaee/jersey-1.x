/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.spring.jerseymanaged;

import com.sun.jersey.spring.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.WebResource;

/**
 * Test prototype resources that are not managed by spring (but jersey).<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AutowiredPerRequestResourceTest extends AbstractResourceTest {
    
    private static final String RESOURCE_PATH = "autowiredperrequest";
    
    public AutowiredPerRequestResourceTest() {
        super(false);
    }

    @Test
    public void testGetAndUpdateSingletonItem() {
        
        final WebResource itemResource = resource( RESOURCE_PATH + "/singletonitem" );
        
        final Item actualItem = itemResource.get( Item.class );
        Assert.assertNotNull( actualItem );
        Assert.assertEquals( actualItem.getValue(), TestData.MANAGED );

        /* update the value of the singleton item and afterwards check if it's the same
         */
        final String newValue = "newValue";
        final WebResource itemValueResource = resource( RESOURCE_PATH + "/singletonitem/value/" + newValue );
        itemValueResource.put();
        
        final Item actualUpdatedItem = itemResource.get( Item.class );
        Assert.assertNotNull( actualUpdatedItem );
        Assert.assertEquals( actualUpdatedItem.getValue(), newValue );
        
    }
    
    @Test
    public void testGetAndUpdatePrototypeItem() {
        
        final WebResource itemResource = resource( RESOURCE_PATH + "/prototypeitem" );
        final Item2 actualItem = itemResource.get( Item2.class );
        Assert.assertNotNull( actualItem );
        Assert.assertEquals( actualItem.getValue(), TestData.MANAGED );
        
        /* update the value of the prototype item and afterwards check that it's not the same
         */
        final String newValue = "newValue";
        final WebResource itemValueResource = resource( RESOURCE_PATH + "/prototypeitem/value/" + newValue );
        itemValueResource.put();
        
        final Item2 actualUpdatedItem = itemResource.get( Item2.class );
        Assert.assertNotNull( actualUpdatedItem );
        Assert.assertEquals( actualUpdatedItem.getValue(), TestData.MANAGED );
        
    }
    
    @Test
    public void testGetAndUpdateCount() {
        
        final WebResource countResource = resource( RESOURCE_PATH + "/count" );
        
        /* the count has to be the same for each request, even if one request
         * changed the count
         */
        final int actualCount = Integer.parseInt( countResource.get( String.class ) );
        countResource.post();
        final int actualCountUpdated = Integer.parseInt( countResource.get( String.class ) );
        Assert.assertEquals( actualCountUpdated, actualCount );
        
    }

}
