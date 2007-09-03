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
import com.sun.ws.rest.samples.jsonfromjaxb.jaxb.ObjectFactory;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;

/**
 * @author Jakub Podlesak
 */
@UriTemplate(value = "/flights/")
public class FlightList {

    Flights myFlights;

    public FlightList() {
    }

    @HttpMethod(value = "GET")
    @ProduceMime({"application/json", "application/xml"})
    public Flights getFlightList() {
        return getFlights();
    }

    @HttpMethod(value = "PUT")
    @ConsumeMime({"application/json", "application/xml"})
    public void putFlightListAsXml(Flights flights) {
        setFlights(flights);
    }

    private synchronized void setFlights(Flights flights) {
        myFlights = flights;
    }

    private synchronized Flights getFlights() {
        if (null == myFlights) {
            myFlights = (new ObjectFactory()).createFlights();
            FlightType fligth123 = new FlightType();
            fligth123.setCompany("Czech Airlines");
            fligth123.setNumber(123);
            fligth123.setFlightId("OK123");
            fligth123.setAircraft("B737");
            FlightType fligth124 = new FlightType();
            fligth124.setCompany("Czech Airlines");
            fligth124.setNumber(124);
            fligth124.setFlightId("OK124");
            fligth124.setAircraft("AB115");
            myFlights.getFlight().add(fligth123);
            myFlights.getFlight().add(fligth124);
        }
        return myFlights;
    }
}
