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



package com.sun.jersey.samples.jaxws;

import com.sun.jersey.api.container.ContainerFactory;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.http.HTTPBinding;

/**
 *
 * @author Doug Kohlert
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        /** Create a JAX-WS Provider */
        Provider provider = ContainerFactory.createContainer(Provider.class);
        
        /** Create a JAX-WS Endpoint with the provider */
        Endpoint endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING, provider);
        /** publish the endpoint */
        endpoint.publish("http://localhost:9998/endpoint");
        
        System.out.println("JAX-WS endpoint running, visit: http://127.0.0.1:9998/endpoint/start, hit return to stop...");
        /** wait of for a CR */
        System.in.read();
        System.out.println("Stopping JAX-WS endpoint");
        
        /* stop the endpoint */
        endpoint.stop();
        System.out.println("Server stopped");        
    }
    
}
