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


import javax.naming.InitialContext;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class ShoppingApplication extends Application
{

   @Override
   public Set<Class<?>> getClasses()
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(EntityNotFoundExceptionMapper.class);
      classes.add(EJBExceptionMapper.class);
      return classes;
   }

   @Override
   public Set<Object> getSingletons()
   {
      HashSet<Object> set = new HashSet();
      try
      {
         InitialContext ctx = new InitialContext();

         Object obj = ctx.lookup(
                 "java:comp/env/ejb/CustomerResource");
         set.add(obj);

         obj = ctx.lookup(
                 "java:comp/env/ejb/OrderResource");
         set.add(obj);

         obj = ctx.lookup(
                 "java:comp/env/ejb/ProductResource");
         set.add(obj);

         obj = ctx.lookup(
                 "java:comp/env/ejb/StoreResource");
         set.add(obj);

      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
      return set;
   }
}
