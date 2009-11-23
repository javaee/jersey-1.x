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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customer")
public class Customer
{
   private int id;
   private String firstName;
   private String lastName;
   private String street;
   private String city;
   private String state;
   private String zip;
   private String country;

   @XmlAttribute
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   @XmlElement(name = "first-name")
   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   @XmlElement(name = "last-name")
   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   @XmlElement
   public String getStreet()
   {
      return street;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   @XmlElement
   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   @XmlElement
   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   @XmlElement
   public String getZip()
   {
      return zip;
   }

   public void setZip(String zip)
   {
      this.zip = zip;
   }

   @XmlElement
   public String getCountry()
   {
      return country;
   }

   public void setCountry(String country)
   {
      this.country = country;
   }
}
