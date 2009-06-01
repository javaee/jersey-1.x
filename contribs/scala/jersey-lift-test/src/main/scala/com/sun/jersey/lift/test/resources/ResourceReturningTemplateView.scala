package com.sun.jersey.lift.test.resources


import javax.ws.rs.{Produces, Path, GET}

/**
 * @version $Revision: 1.1 $
 */
@Path("/resourceReturningTemplateView")
class ResourceReturningTemplateView{
    @GET
    def view() = <lift:surround with="default" at="content">
  <h2 class="mytitle">Embedded Lift Template inside JAXRS bean</h2>
  <p><lift:helloWorld.howdy /></p>
</lift:surround>

}
