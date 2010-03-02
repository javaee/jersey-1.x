
package com.sun.jersey.osgi.rdtestbundle;


import javax.ws.rs.*;
/**
 *
 * @author japod
 */
@Path("/simple")
public class SimpleResource {

    @GET public String getMe() {
        return "jersey resource";
    }
}
