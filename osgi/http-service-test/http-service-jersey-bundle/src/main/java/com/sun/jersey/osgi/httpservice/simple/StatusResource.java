
package com.sun.jersey.osgi.httpservice.simple;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author japod
 */
@Path("status")
public class StatusResource {

    @GET @Produces("text/plain")
    public String getStatus() {
        return "active";
    }

}
