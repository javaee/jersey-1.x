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

import com.restfully.shop.domain.Link;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StoreResourceBean implements StoreResource
{
   public Response head(UriInfo uriInfo)
   {
      UriBuilder absolute = uriInfo.getBaseUriBuilder();
      String customerUrl = absolute.clone().path("customers").build().toString();
      String orderUrl = absolute.clone().path("orders").build().toString();
      String productUrl = absolute.clone().path("products").build().toString();

      Response.ResponseBuilder builder = Response.ok();
      builder.header("Link", new Link("customers", customerUrl, "application/xml"));
      builder.header("Link", new Link("orders", orderUrl, "application/xml"));
      builder.header("Link", new Link("products", productUrl, "application/xml"));
      return builder.build();
   }
}
