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

package com.sun.ws.rest.impl.client;

import com.sun.ws.rest.impl.client.urlconnection.URLConnectionClientHandler;
import java.net.URI;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Client extends Filterable implements ClientHandler {
    public Client(ClientHandler root) {
        super(root);
    }
        
    public final ResourceProxy proxy(String u) {
        return proxy(URI.create(u));
    }
    
    public final ResourceProxy proxy(URI u) {
        return new ResourceProxy(this, u);
    }
    
    // ClientHandler
    
    public ClientResponse handle(ClientRequest ro) {
        return getHeadHandler().handle(ro);
    }

    
    public static Client create() {
        return new Client(new URLConnectionClientHandler());
    }
}