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
package com.sun.jersey.samples.jsonfromjaxb.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.samples.jsonfromjaxb.config.JAXBContextResolver;
import com.sun.jersey.samples.jsonfromjaxb.jaxb.Flights;
import java.util.Formatter;

/**
 *
 * @author japod
 */
public class Main {

    static final String UrlBase = "http://localhost:8080/JsonFromJaxb/resources/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            // create a client:
            
            ClientConfig cc = new DefaultClientConfig();
            // use the following jaxb context resolver
            cc.getProviderClasses().add(JAXBContextResolver.class);
            Client c = Client.create(cc);
            
            WebResource wr = c.resource(UrlBase);
            
            
            // get the initial representation
            
            Flights flights = wr.path("flights/").accept("application/json").get(Flights.class);
            
            // and print it out
            
            System.out.println(new Formatter().format("List of flights found:\n%s", flights.toString()));
            
            // update the list:
                        
            // remove the second row
            if (flights.getFlight().size() > 1) {
                flights.getFlight().remove(1);
            }
            
            // update the first one
            flights.getFlight().get(0).setNumber(125);
            flights.getFlight().get(0).setFlightId("OK125");
            
            
            // and send the updated list back to the server
            wr.path("flights/").type("application/json").put(flights);
            

            // get the updated list out from the server:
            Flights updatedFlights = wr.path("flights/").accept("application/json").get(Flights.class);
            // and print it out:
            System.out.println(new Formatter().format("List of updated flights:\n%s", updatedFlights.toString()));
            
        } catch (Exception e) {
            System.out.println("TEST FAILED! :-(");
            e.printStackTrace(System.out);
        }
    }
}
