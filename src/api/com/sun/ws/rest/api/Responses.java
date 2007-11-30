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

import javax.ws.rs.core.Response;

/**
 * Common status codes and responses.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class Responses {
    public static final int NO_CONTENT = 204;
    
    public static final int NOT_MODIFIED = 304;
    
    public static final int NOT_FOUND = 404;
    
    public static final int METHOD_NOT_ALLOWED = 405;
    
    public static final int NOT_ACCEPTABLE = 406;
    
    public static final int CONFLICT = 409;
    
    public static final int PRECONDITION_FAILED = 412;
    
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    
    public static Response noContent() {
        return status(NO_CONTENT);
    }
    
    public static Response notModified() {
        return status(NOT_MODIFIED);
    }
    
    public static Response notFound() {
        return status(NOT_FOUND);
    }
    
    public static Response methodNotAllowed() {
        return status(METHOD_NOT_ALLOWED);
    }
    
    public static Response notAcceptable() {
        return status(NOT_ACCEPTABLE);
    }
    
    public static Response conflict() {
        return status(CONFLICT);        
    }
    
    public static Response preconditionFailed() {
        return status(PRECONDITION_FAILED);
    }
    
    public static Response unsupportedMediaType() {
        return status(UNSUPPORTED_MEDIA_TYPE);
    }
    
    private static Response status(int status) {
        return Response.serverError().status(status).build();                
    }
}
