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

package com.sun.jersey.samples.helloworld;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.*;
import com.sun.jersey.impl.container.grizzly.*;
import java.util.*;
import java.io.IOException;

public class Main {
    
    public static void main(String[] args) throws IOException {
        
        final String baseUri = "http://localhost:9998/";
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        initParams.put("com.sun.jersey.config.property.packages", "com.sun.jersey.samples.helloworld.resources");

        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        System.out.println(String.format("Jersey app started with WADL available at %sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...", baseUri, baseUri));
        System.in.read();
        threadSelector.stopEndpoint();
        System.exit(0);
    }    
}
