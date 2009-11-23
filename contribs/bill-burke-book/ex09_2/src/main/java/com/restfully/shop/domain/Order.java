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
package com.restfully.shop.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@XmlRootElement(name = "order")
@XmlType(propOrder = {"total", "date", "cancelled", "customer", "lineItems"})
public class Order
{
   protected int id;
   protected boolean cancelled;
   protected List<LineItem> lineItems;
   protected String total;
   protected String date;
   protected Customer customer;

   @XmlAttribute
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public boolean isCancelled()
   {
      return cancelled;
   }

   public void setCancelled(boolean cancelled)
   {
      this.cancelled = cancelled;
   }

   @XmlElementWrapper(name = "line-items")
   public List<LineItem> getLineItems()
   {
      return lineItems;
   }

   public void setLineItems(List<LineItem> lineItems)
   {
      this.lineItems = lineItems;
   }

   public String getDate()
   {
      return date;
   }

   public void setDate(String date)
   {
      this.date = date;
   }

   public String getTotal()
   {
      return total;
   }

   public void setTotal(String total)
   {
      this.total = total;
   }

   @XmlElementRef
   public Customer getCustomer()
   {
      return customer;
   }

   public void setCustomer(Customer customer)
   {
      this.customer = customer;
   }
}
