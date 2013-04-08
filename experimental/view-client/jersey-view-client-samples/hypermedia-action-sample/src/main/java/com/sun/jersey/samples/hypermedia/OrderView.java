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
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.api.client.WebResourceLinkHeaders;
import com.sun.jersey.client.view.annotation.Status;
import com.sun.jersey.samples.hypermedia.client.model.Address;
import com.sun.jersey.samples.hypermedia.client.model.Order;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

public class OrderView {

    private Order order;

    private Client c;

    private ViewResource r;

    private WebResourceLinkHeaders links;

    @GET
    @Status(200)
    @Consumes("*/*")
    public void build(Order order, 
            @Context Client c,
            @Context ViewResource r,
            @Context WebResourceLinkHeaders links) {
        this.order = order;
        this.c = c;
        this.r = r;
        this.links = links;
    }

    
    public Order getOrder() {
        return order;
    }

    // operations

    public OrderView update(Order o) {
        return r.put(new Update<OrderView>(OrderView.class, r), o).
                view();
    }

    // relation
    
    public CustomerView getCustomer() {
        return c.view(getOrder().getCustomer(), CustomerView.class);
    }
    
    // actions

    public OrderView review(String notes) {
        return links.viewResource("review").
                header("notes", notes).
                post(new Refresh<OrderView>(OrderView.class)).
                view();
    }

    public OrderView pay(String newCardNumber) {
        return links.viewResource("pay").
                queryParam("newCardNumber", newCardNumber).
                post(new Refresh<OrderView>(OrderView.class)).
                view();
    }

    public OrderView ship(Address newShippingAddress) {
        return links.viewResource("ship").
                put(new Refresh<OrderView>(OrderView.class), newShippingAddress).
                view();
    }

    public OrderView cancel(String notes) {
        return links.viewResource("cancel").
                queryParam("notes", notes).
                post(new Refresh<OrderView>(OrderView.class)).
                view();
    }
}
