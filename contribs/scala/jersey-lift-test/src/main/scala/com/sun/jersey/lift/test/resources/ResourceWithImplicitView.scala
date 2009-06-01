package com.sun.jersey.lift.test.resources


import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.{GET, Path}
import scala.xml.NodeSeq

/**
 * @version $Revision: 1.1 $
 */
@Path("/resourceWithImplicitView")
@ImplicitProduces(Array("text/html;qs=5"))
class ResourceWithImplicitView {

  @GET
  def view() = this
}