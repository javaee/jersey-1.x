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

import javax.ejb.EJBException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException>
{
   @Context
   private Providers providers;

   public Response toResponse(EJBException exception)
   {
      if (exception.getCausedByException() == null)
      {
         return Response.serverError().build();
      }
      Class cause = exception.getCausedByException().getClass();
      ExceptionMapper mapper = providers.getExceptionMapper(cause);
      if (mapper == null)
      {
         return Response.serverError().build();
      }
      else
      {
         return mapper.toResponse(exception.getCausedByException());
      }
   }
}
