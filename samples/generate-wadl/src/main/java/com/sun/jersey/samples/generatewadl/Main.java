/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.samples.generatewadl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.server.wadl.generators.WadlGeneratorApplicationDoc;
import com.sun.jersey.server.wadl.generators.WadlGeneratorGrammarsSupport;
import com.sun.jersey.server.wadl.generators.resourcedoc.WadlGeneratorResourceDocSupport;
import com.sun.jersey.samples.generatewadl.resources.ItemsResource;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Jul 27, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class Main {
    
    public static void main(String[] args) throws IOException {
        
        final Map<String, Object> props = new HashMap<String, Object>();
        props.put( PackagesResourceConfig.PROPERTY_PACKAGES, ItemsResource.class.getPackage().getName() );
        
        final WadlGeneratorConfig config = WadlGeneratorConfig
            .generator( WadlGeneratorApplicationDoc.class )
            .prop( "applicationDocsFile", "classpath:/application-doc.xml" )
            .generator( WadlGeneratorGrammarsSupport.class )
            .prop( "grammarsFile", "classpath:/application-grammars.xml" )
            .generator( WadlGeneratorResourceDocSupport.class )
            .prop( "resourceDocFile", "classpath:/resourcedoc.xml" )
            .build();
        
        props.put( ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, config );
        
        final ResourceConfig resourceConfig = new PackagesResourceConfig( props );
        
        final HttpHandler container = ContainerFactory.createContainer( HttpHandler.class, resourceConfig );
        
        final HttpServer server = HttpServerFactory.create( "http://localhost:9998/", container );
        server.start();
        
        System.out.println("Server running");
        System.out.println("Visit: http://localhost:9998/items");
        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");   
        server.stop(0);
        System.out.println("Server stopped");
    }
    
}
