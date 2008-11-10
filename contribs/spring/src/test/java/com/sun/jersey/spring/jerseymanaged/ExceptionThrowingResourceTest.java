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

import javax.ws.rs.core.Response.Status;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spring.AbstractResourceTest;

/**
 * Check, that exceptions thrown by resource methods are propagated to the container.
 * This is the test case for issue 81: SpringServlet failing to propagate unchecked exceptions.
 * 
 * @see <a href="https://jersey.dev.java.net/issues/show_bug.cgi?id=81">Issue 81</a>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Test
public class ExceptionThrowingResourceTest extends AbstractResourceTest {
    
    public ExceptionThrowingResourceTest() {
    }

    /* (non-Javadoc)
     * @see com.sun.ws.rest.spring.AutowiredSingletonResourceTestBase#testGetAndUpdateCount()
     */
    @Test
    public void testGetAndUpdateItem() {
        
        final WebResource itemResource = resource( "test-issue-81" );
        final ClientResponse response = itemResource.get( ClientResponse.class );
        Assert.assertNotNull( response );
        Assert.assertEquals( response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode() );
        
    }

}
