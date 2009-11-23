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

package com.restfully.shop.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Entity(name = "Product")
public class ProductEntity
{
   private int id;
   private String name;
   private double cost;

   @Id
   @GeneratedValue
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public double getCost()
   {
      return cost;
   }

   public void setCost(double cost)
   {
      this.cost = cost;
   }
}
