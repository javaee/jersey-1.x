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

import javax.ws.rs.Path;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spring.AbstractResourceTest;
import com.sun.jersey.spring.Item;
import com.sun.jersey.spring.TestData;


/**
 * Test resources that use spring annotations and are not defined in applicationContext.xml.<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Test
public class AnnotatedPerRequestResourceTest extends AbstractResourceTest {
    
    private final String _resourcePath;

    public AnnotatedPerRequestResourceTest() {
        _resourcePath = AnnotatedPerRequestResource.class.getAnnotation( Path.class ).value();
    }
    
    @Test
    public void testGetAndUpdateManagedSingletonItem() {
        
        final WebResource itemResource = resource( _resourcePath + "/definedsingletonitem" );
        
        final Item actualItem = itemResource.get( Item.class );
        Assert.assertNotNull( actualItem );
        Assert.assertEquals( actualItem.getValue(), TestData.MANAGED );
        
    }
    
    @Test
    public void testGetSpringComponent() {
        
        final WebResource itemResource = resource( _resourcePath + "/annotatedspringcomponent" );
        
        final SpringComponent actualItem = itemResource.get( SpringComponent.class );
        Assert.assertNotNull( actualItem );
        
    }

}
