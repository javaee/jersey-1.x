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


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.InitialContext;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class ShoppingApplication extends Application
{
   private Set<Class<?>> classes = new HashSet<Class<?>>();

   public ShoppingApplication()
   {
      classes.add(EntityNotFoundExceptionMapper.class);
   }

   public Set<Class<?>> getClasses()
   {
      return classes;
   }

   protected ApplicationContext springContext;

   public Set<Object> getSingletons()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         String xmlFile = (String) ctx.lookup(
                 "java:comp/env/spring-beans-file");
         springContext = new ClassPathXmlApplicationContext(xmlFile);


      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
      HashSet<Object> set = new HashSet();
      set.add(springContext.getBean("customer"));
      set.add(springContext.getBean("order"));
      set.add(springContext.getBean("product"));
      set.add(springContext.getBean("store"));
      return set;
   }

}
