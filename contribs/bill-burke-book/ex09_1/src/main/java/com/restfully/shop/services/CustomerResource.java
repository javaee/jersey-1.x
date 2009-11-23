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

import com.restfully.shop.domain.Customer;
import com.restfully.shop.domain.Customers;
import com.restfully.shop.domain.Link;
// import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/customers")
public class CustomerResource
{
   private Map<Integer, Customer> customerDB = new ConcurrentHashMap<Integer, Customer>();
   private AtomicInteger idCounter = new AtomicInteger();

   public CustomerResource()
   {
      Customer customer;
      int id = 1;

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Bill");
      customer.setLastName("Burke");
      customer.setStreet("263 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Joe");
      customer.setLastName("Burke");
      customer.setStreet("264 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Monica");
      customer.setLastName("Burke");
      customer.setStreet("265 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Steve");
      customer.setLastName("Burke");
      customer.setStreet("266 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Rod");
      customer.setLastName("Burke");
      customer.setStreet("267 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Bob");
      customer.setLastName("Burke");
      customer.setStreet("268 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);
   }

   @POST
   @Consumes("application/xml")
   public Response createCustomer(Customer customer)
   {
      customer.setId(idCounter.incrementAndGet());
      customerDB.put(customer.getId(), customer);
      System.out.println("Created customer " + customer.getId());
      return Response.created(URI.create("/customers/" + customer.getId())).build();

   }

   @GET
   @Produces("application/xml")
   // @Formatted
   public Customers getCustomers(@QueryParam("start") int start,
                                 @QueryParam("size") @DefaultValue("2") int size,
                                 @Context UriInfo uriInfo)
   {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      builder.queryParam("start", "{start}");
      builder.queryParam("size", "{size}");

      ArrayList<Customer> list = new ArrayList<Customer>();
      ArrayList<Link> links = new ArrayList<Link>();
      synchronized (customerDB)
      {
         int i = 0;
         for (Customer customer : customerDB.values())
         {
            if (i >= start && i < start + size) list.add(customer);
            i++;
         }
         // next link
         if (start + size < customerDB.size())
         {
            int next = start + size;
            URI nextUri = builder.clone().build(next, size);
            Link nextLink = new Link("next", nextUri.toString(), "application/xml");
            links.add(nextLink);
         }
         // previous link
         if (start > 0)
         {
            int previous = start - size;
            if (previous < 0) previous = 0;
            URI previousUri = builder.clone().build(previous, size);
            Link previousLink = new Link("previous", previousUri.toString(), "application/xml");
            links.add(previousLink);
         }
      }
      Customers customers = new Customers();
      customers.setCustomers(list);
      customers.setLinks(links);
      return customers;
   }

   @GET
   @Path("{id}")
   @Produces({"application/xml", "application/json"})
   public Customer getCustomer(@PathParam("id") int id)
   {
      Customer customer = customerDB.get(id);
      if (customer == null)
      {
         throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      return customer;
   }

}
