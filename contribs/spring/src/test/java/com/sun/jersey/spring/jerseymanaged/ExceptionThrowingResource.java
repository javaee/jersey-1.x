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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.resource.Singleton;

/**
 * A singleton resource class that throws an exception in it's single resource method,
 * to test issue 81: SpringServlet failing to propagate unchecked exceptions.
 * 
 * @see <a href="https://jersey.dev.java.net/issues/show_bug.cgi?id=81">Issue 81</a>
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Path("test-issue-81")
@Singleton
public class ExceptionThrowingResource {
    
    public ExceptionThrowingResource() {
    }

    @GET
    @Produces( "application/xml" )
    public Response getItem() {
        if ( true ) {
            throw new RuntimeException( "this exception should be visible in the logs and MUST result in status 500 on the client side." );
        }
        return null;
    }
    
}
