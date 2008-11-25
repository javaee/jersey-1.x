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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import com.sun.jersey.spring.AbstractResourceTest;

/**
 * Test that injection (of jersey) also works for proxied spring beans.
 * In detail this tests {@link SpringComponentProviderFactory#getInjectableInstance}.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Test
public class ProxiedResourceTest extends AbstractResourceTest {
    
    private String _resourcePath;

    public ProxiedResourceTest() {
        _resourcePath = "proxiedresource";
    }
    
    @Test( enabled=true )
    public void testGetAndUpdateItem() {

        {
        final WebResource itemResource = resource( _resourcePath );
        final String actualItem = itemResource.get( String.class );
        Assert.assertNotNull( actualItem );
        Assert.assertEquals( actualItem, getBaseUri() );
        }

        {
        final WebResource itemResource = resource( _resourcePath + "/subresource");
        final String actualItem = itemResource.get( String.class );
        Assert.assertNotNull( actualItem );
        Assert.assertEquals( actualItem, getBaseUri() + _resourcePath + "/subresource");
        }
    }

}
