/*
 * $Id: $ (c)
 * Copyright 2008 freiheit.com technologies GmbH
 *
 * Created on Jul 27, 2008
 *
 * This file contains unpublished, proprietary trade secret information of
 * freiheit.com technologies GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * freiheit.com technologies GmbH.
 */
package com.sun.jersey.samples.generatewadl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
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
        
//        final WadlGeneratorConfig config = WadlGeneratorConfig
//            .generator( WadlGeneratorApplicationDoc.class.getName() )
//            .prop( "applicationDocsFile", "classpath:/application-doc.xml" )
//            .add()
//            .generator( WadlGeneratorGrammarsSupport.class.getName() )
//            .prop( "grammarsFile", "classpath:/application-grammars.xml" )
//            .add()
//            .generator( WadlGeneratorResourceDocSupport.class.getName() )
//            .prop( "resourceDocFile", "classpath:/resourcedoc.xml" )
//            .add()
//            .build();
//        
//        props.put( ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, config );
        
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
