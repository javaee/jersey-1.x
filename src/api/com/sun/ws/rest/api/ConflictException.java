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

package com.sun.ws.rest.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A HTTP 409 (Conflict) exception.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ConflictException extends WebApplicationException {
   
    /**
     * Create a HTTP 409 (Conflict) exception.
     */
    public ConflictException() {
        super(Response.Builder.serverError().status(409).build());
    }
    
    /**
     * Create a HTTP 409 (Conflict) exception.
     * @param message the String that is the entity of the 409 response.
     */
    public ConflictException(String message) {
        super(Response.Builder.serverError().status(409).
                entity(message).type("text/plain").build());
    }    
}
