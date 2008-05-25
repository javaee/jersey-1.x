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

package com.sun.jersey.api.client;

/**
 * A client filter capable of modifying the outbound HTTP request or 
 * the inbound HTTP response. 
 * <p>
 * An application-based filter extends this class and implements the 
 * {@link ClientHandler#handle} method. The general implementation pattern
 * is as follows:
 * <blockquote><pre>
 *     class AppClientFilter extends ClientFilter {
 *
 *         public ClientResponse handle(ClientRequest cr) {
 *             // Modify the request
 *             ClientRequest mcr = modifyRequest(cr);
 * 
 *             // Call the next client handler in the filter chain
 *             ClientResponse resp = getNext(mcr);
 * 
 *             // Modify the response
 *             return modifyResponse(resp);
 *         }
 *
 *         private byte[] loadClassData(String name) {
 *             // load the class data from the connection
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote>
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ClientFilter implements ClientHandler {
    private ClientHandler next;
    
    /* package */ final void setNext(ClientHandler next) {
        this.next = next;
    }
   
    /**
     * Get the next client handler to invoke in the chain
     * of filters.
     * 
     * @return the next client handler.
     */
    public final ClientHandler getNext() {
        return next;
    }
    
    public abstract ClientResponse handle(ClientRequest cr) 
            throws ClientHandlerException;
}
