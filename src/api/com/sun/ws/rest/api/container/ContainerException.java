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

package com.sun.ws.rest.api.container;

/**
 * Runtime exception to be caught by the container.
 * <p>
 * This exception may be thrown by the application signaling that the 
 * container should handle the exception to produce an appropriate HTTP response.
 * <p>
 * This exception may also be thrown by the runtime if an exception  
 * occurs that should be handled by the container.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContainerException extends RuntimeException {

    /**
     * Construct a new instance with the supplied message
     */
    public ContainerException() {
        super();
    }

    /**
     * Construct a new instance with the supplied message
     * @param message the message
     */
    public ContainerException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with the supplied message and cause
     * @param message the message
     * @param cause the Throwable that caused the exception to be thrown
     */
    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
        
    /**
     * Construct a new instance with the supplied cause
     * @param cause the Throwable that caused the exception to be thrown
     */
    public ContainerException(Throwable cause) {
        super(cause);
    }
}
