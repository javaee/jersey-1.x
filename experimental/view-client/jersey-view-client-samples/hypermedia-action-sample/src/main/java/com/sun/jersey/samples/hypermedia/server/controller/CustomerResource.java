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

package com.sun.jersey.samples.hypermedia.server.controller;

import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.ContextualActionSet;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.*;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import static com.sun.jersey.samples.hypermedia.server.model.Customer.Status.*;

/**
 * CustomerResource class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@Path("/customers/{id}")
@HypermediaController(
    model=Customer.class,
    linkType=LinkType.LINK_HEADERS
    )
public class CustomerResource {

    private Customer customer;

    public CustomerResource(@PathParam("id") String id) {
        customer = DB.customers.get(id);
        if (customer == null) {
            throw new WebApplicationException(404);     // not found
        }
    }

    @GET
    @Produces("application/xml")
    public Customer getCustomer(@PathParam("id") String id) {
        return customer;
    }

    @PUT
    @Consumes("application/xml")
    public void putCustomer(@PathParam("id") String id, Customer customer) {
        assert id.equals(customer.getId());
        this.customer = customer;
        DB.customers.put(id, customer);
    }

    @GET
    @Path("/address/{aid}")
    public Address getAddress(@PathParam("id") String id,
            @PathParam("aid") String aid) {
        return DB.customers.get(id).getAddressById(aid);
    }
    
    // -- Actions and ActionSets ------------------------------------
    //
    // Set a customer's state as ACTIVE or SUSPENDED.
    // For simplicity, these actions are implemented by updating the
    // customer's status. Note that this could be done also using
    // @PUT. In general, these actions may involve several steps (a
    // workflow) that cannot be easily translated into a single @PUT
    // action by the client.
    //

    @GET
    @Action("refresh") @Path("refresh")
    @Produces("application/xml")
    public Customer refresh(@PathParam("id") String id) {
        return getCustomer(id);
    }

    @PUT
    @Action("update") @Path("update")
    @Consumes("application/xml")
    public void update(@PathParam("id") String id, Customer c) {
        putCustomer(id, c);
    }

    @POST
    @Action("activate") @Path("activate")
    public void activate() {
        customer.setStatus(ACTIVE);
    }

    @POST
    @Action("suspend") @Path("suspend")
    public void suspend() {
        customer.setStatus(SUSPENDED);
    }

    @ContextualActionSet
    public Set<String> getContextualActionSet() {
        Set<String> result = new HashSet<String>();
        result.add("refresh");
        result.add("update");
        switch (customer.getStatus()) {
            case ACTIVE:
                result.add("suspend");      // @Action's value
                break;
            case SUSPENDED:
                result.add("activate");     // @Action's value
                break;
        }
        return result;
    }

}
