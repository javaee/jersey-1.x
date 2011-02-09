/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
public class AnnotatedSingletonResourceTest extends AbstractResourceTest {
    
    private final String _resourcePath;

    public AnnotatedSingletonResourceTest() {
        _resourcePath = AnnotatedSingletonResource.class.getAnnotation( Path.class ).value();
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
