/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.samples.jsonfromjaxb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.samples.jsonfromjaxb.config.JAXBContextResolver;
import com.sun.jersey.samples.jsonfromjaxb.jaxb.AircraftType;
import com.sun.jersey.samples.jsonfromjaxb.jaxb.Flights;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpServer;

import java.util.List;
/**
 *
 * @author japod
 */
public class MainTest extends TestCase {
    
    private HttpServer httpServer;
    
    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        httpServer = Main.startServer();

        ClientConfig cc = new DefaultClientConfig();
        // use the following jaxb context resolver
        cc.getClasses().add(JAXBContextResolver.class);
        Client c = Client.create(cc);
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        httpServer.stop();
    }

    /**
     * Test checks that the application.wadl is reachable.
     */
    public void testApplicationWadl() {
        String applicationWadl = r.path("application.wadl").get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0",
                applicationWadl.length() > 0);
    }

    /**
     * Test check GET on the "flights" resource in "application/json" format.
     */
    public void testGetOnFlightsJSONFormat() {
        // get the initial representation
        Flights flights = r.path("flights").
                accept("application/json").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found",
                2, flights.getFlight().size());
    }

    /**
     * Test checks PUT on the "flights" resource in "application/json" format.
     */
    public void testPutOnFlightsJSONFormat() {
        // get the initial representation
        Flights flights = r.path("flights").
                accept("application/json").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found",
                2, flights.getFlight().size());

        // remove the second flight entry
        if (flights.getFlight().size() > 1) {
            flights.getFlight().remove(1);
        }

        // update the first entry
        flights.getFlight().get(0).setNumber(125);
        flights.getFlight().get(0).setFlightId("OK125");

        // and send the updated list back to the server
        r.path("flights").type("application/json").put(flights);

        // get the updated list out from the server:
        Flights updatedFlights = r.path("flights").
                accept("application/json").get(Flights.class);
        //check that there is only one flight entry
        assertEquals("Remaining number of flight entries do not match the expected value",
                1, updatedFlights.getFlight().size());
        // check that the flight entry in retrieved list has FlightID OK!@%
        assertEquals("Retrieved flight ID doesn't match the expected value",
                "OK125", updatedFlights.getFlight().get(0).getFlightId());
    }

    /**
     * Test checks GET on "flights" resource with mime-type "application/xml".
     */
    public void testGetOnFlightsXMLFormat() {
        // get the initial representation
        Flights flights = r.path("flights").
                accept("application/xml").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found",
                2, flights.getFlight().size());
    }

    /**
     * Test checks PUT on "flights" resource with mime-type "application/xml".
     */
    public void testPutOnFlightsXMLFormat() {
        // get the initial representation
        Flights flights = r.path("flights").
                accept("application/XML").get(Flights.class);
        // check that there are two flight entries
        assertEquals("Expected number of initial entries not found",
                2, flights.getFlight().size());

        // remove the second flight entry
        if (flights.getFlight().size() > 1) {
            flights.getFlight().remove(1);
        }

        // update the first entry
        flights.getFlight().get(0).setNumber(125);
        flights.getFlight().get(0).setFlightId("OK125");

        // and send the updated list back to the server
        r.path("flights").type("application/XML").put(flights);

        // get the updated list out from the server:
        Flights updatedFlights = r.path("flights").
                accept("application/XML").get(Flights.class);
        //check that there is only one flight entry
        assertEquals("Remaining number of flight entries do not match the expected value",
                1, updatedFlights.getFlight().size());
        // check that the flight entry in retrieved list has FlightID OK!@%
        assertEquals("Retrieved flight ID doesn't match the expected value",
                "OK125", updatedFlights.getFlight().get(0).getFlightId());
    }

    /**
     * Test check GET on the "aircrafts" resource in "application/json" format.
     */
    public void testGetOnAircraftsJSONFormat() {
        GenericType<List<AircraftType>> genericType =
                new GenericType<List<AircraftType>>() {};
        // get the initial representation
        List<AircraftType> aircraftTypes = r.path("aircrafts").
                accept("application/json").get(genericType);
        // check that there are two aircraft type entries
        assertEquals("Expected number of initial aircraft types not found",
                2, aircraftTypes.size());
    }

}
