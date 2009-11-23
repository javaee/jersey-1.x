/*
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 */
package com.restfully.shop.services;

import com.restfully.shop.domain.Order;
//import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Path("/orders")
public interface OrderResource
{
   @POST
   @Consumes("application/xml")
   Response createOrder(Order order, @Context UriInfo uriInfo);

   @POST
   @Path("purge")
   void purgeOrders();

   @HEAD
   @Produces("application/xml")
   Response getOrdersHeaders(@Context UriInfo uriInfo);

   @GET
   @Produces("application/xml")
//   @Formatted
   Response getOrders(@QueryParam("start") int start,
                      @QueryParam("size") @DefaultValue("2") int size,
                      @Context UriInfo uriInfo);

   @POST
   @Path("{id}/cancel")
   void cancelOrder(@PathParam("id") int id);

   @GET
   @Path("{id}")
   @Produces("application/xml")
   Response getOrder(@PathParam("id") int id, @Context UriInfo uriInfo);

   @HEAD
   @Path("{id}")
   @Produces("application/xml")
   Response getOrderHeaders(@PathParam("id") int id, @Context UriInfo uriInfo);
}
