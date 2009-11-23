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

package com.restfully.shop.domain;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@XmlRootElement(name = "orders")
public class Orders
{
   protected Collection<Order> orders;
   protected List<Link> links;

   @XmlElementRef
   public Collection<Order> getOrders()
   {
      return orders;
   }

   public void setOrders(Collection<Order> orders)
   {
      this.orders = orders;
   }

   @XmlElementRef
   public List<Link> getLinks()
   {
      return links;
   }

   public void setLinks(List<Link> links)
   {
      this.links = links;
   }

   @XmlTransient
   public String getNext()
   {
      if (links == null) return null;
      for (Link link : links)
      {
         if ("next".equals(link.getRelationship())) return link.getHref();
      }
      return null;
   }

   @XmlTransient
   public String getPrevious()
   {
      if (links == null) return null;
      for (Link link : links)
      {
         if ("previous".equals(link.getRelationship())) return link.getHref();
      }
      return null;
   }

}