/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

import com.sun.jersey.server.hypermedia.filter.HypermediaFilterFactory;
import com.sun.jersey.samples.hypermedia.client.model.Address;

import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class Main {

    public static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();

    public static HttpServer startServer() throws IOException {
        ResourceConfig rc = new PackagesResourceConfig(
                "com.sun.jersey.samples.hypermedia.server.controller");

        rc.getFeatures().put(ResourceConfig.FEATURE_FORMATTED, Boolean.TRUE);

        // Register HypermediaFilterFactory 
        rc.getResourceFilterFactories().add(HypermediaFilterFactory.class);

        System.out.println("Starting grizzly...");
        return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
    }

    public static void main(String[] args) throws Exception {
        // Grizzly initialization
        HttpServer httpServer = startServer();

        try {
            // Create Client and configure it
            Client client = new Client();
            client.addFilter(new LoggingFilter());

            // Create order view
            OrderView orderView = client.view("http://localhost:9998/orders/1",
                    OrderView.class);

            // Approve order
            orderView = orderView.review("approve");

            // Activate customer in order 1 (which was suspended)
            CustomerView customerView = orderView.getCustomer();
            if (!customerView.isActive()) {
                customerView.activate();
            }

            // Pay order
            orderView = orderView.pay("123456789");

            // Ship order (returns new order)
            Address newAddress = new Address();
            newAddress.setCity("Springfield");
            newAddress.setStreet("Main");
            orderView.ship(newAddress);
        }
        finally {
            httpServer.stop();
        }
    }
}
