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

package com.sun.ws.rest.impl.response;

import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class Responses {
    public static final Response NOT_MODIFIED = Response.serverError().status(304).build();
    
    public static final Response NOT_FOUND = Response.serverError().status(404).build();
    
    public static final Response METHOD_NOT_ALLOWED = Response.serverError().status(405).build();
    
    public static final Response NOT_ACCEPTABLE = Response.serverError().status(406).build();
    
    public static final Response PRECONDITION_FAILED = Response.serverError().status(412).build();
    
    public static final Response UNSUPPORTED_MEDIA_TYPE = Response.serverError().status(415).build();
}
