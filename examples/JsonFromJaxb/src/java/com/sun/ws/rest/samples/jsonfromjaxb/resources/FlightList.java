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


package com.sun.ws.rest.samples.jsonfromjaxb.resources;

import com.sun.ws.rest.samples.jsonfromjaxb.jaxb.FlightType;
import com.sun.ws.rest.samples.jsonfromjaxb.jaxb.Flights;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

/**
 * @author Jakub Podlesak
 */
@Singleton
@Path(value = "/flights/")
public class FlightList {

    private Flights myFlights;

    /**
     * This class is annotated with @Singleton meaning that only
     * one instance of this class will be instantated per web
     * application. 
     * <p>
     * The flight lists will be constructed just once
     * when the first request to the flight list resource occurs.
     */
    public FlightList() {
        myFlights = new Flights();
        FlightType flight123 = new FlightType();
        flight123.setCompany("Czech Airlines");
        flight123.setNumber(123);
        flight123.setFlightId("OK123");
        flight123.setAircraft("B737");
        FlightType flight124 = new FlightType();
        flight124.setCompany("Czech Airlines");
        flight124.setNumber(124);
        flight124.setFlightId("OK124");
        flight124.setAircraft("AB115");
        myFlights.getFlight().add(flight123);
        myFlights.getFlight().add(flight124);
    }

    @GET
    @ProduceMime({"application/json", "application/xml"})
    public synchronized Flights getFlightList() {
        return myFlights;
    }

    @PUT
    @ConsumeMime({"application/json", "application/xml"})
    public synchronized void putFlightList(Flights flights) {
        myFlights = flights;
    }
}
