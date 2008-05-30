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
