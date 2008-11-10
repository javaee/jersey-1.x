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
public class NameBasedInjectingResourceTest extends AbstractResourceTest {
    
    @Test
    public void testGetInjectedItems() {
        
        /* Get the first item
         */
        final WebResource item1Resource = resource( NameBasedInjectingResource.PATH + "/" + TestData.VAL_1 );
        
        final Item3 actualItem1 = item1Resource.get( Item3.class );
        Assert.assertNotNull( actualItem1 );
        Assert.assertEquals( actualItem1.getValue(), TestData.VAL_1 );

        /* Get the second item
         */
        final WebResource item2Resource = resource( NameBasedInjectingResource.PATH + "/" + TestData.VAL_2 );
        
        final Item3 actualItem2 = item2Resource.get( Item3.class );
        Assert.assertNotNull( actualItem2 );
        Assert.assertEquals( actualItem2.getValue(), TestData.VAL_2 );
        
    }

}
