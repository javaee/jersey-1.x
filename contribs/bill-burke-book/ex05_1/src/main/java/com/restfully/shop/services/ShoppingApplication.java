/**
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

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class ShoppingApplication extends Application
{
   private Set<Object> singletons = new HashSet<Object>();
   private Set<Class<?>> empty = new HashSet<Class<?>>();

   public ShoppingApplication()
   {
      singletons.add(new CustomerResource());
      singletons.add(new CarResource());
   }

   @Override
   public Set<Class<?>> getClasses()
   {
      return empty;
   }

   @Override
   public Set<Object> getSingletons()
   {
      return singletons;
   }
}
