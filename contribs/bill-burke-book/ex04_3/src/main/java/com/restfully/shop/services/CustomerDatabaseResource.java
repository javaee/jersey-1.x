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

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Path("/customers")
public class CustomerDatabaseResource
{

   protected CustomerResource europe = new CustomerResource();
   protected FirstLastCustomerResource northamerica = new FirstLastCustomerResource();

   @Path("{database}-db")
   public Object getDatabase(@PathParam("database") String db)
   {
      if (db.equals("europe"))
      {
         return europe;
      }
      else if (db.equals("northamerica"))
      {
         return northamerica;
      }
      else return null;
   }
}
