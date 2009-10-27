
package com.sun.jersey.glassfish.test.jaxb22;

import com.sun.jersey.api.json.JSONConfiguration;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/myresource")
public class MyResource {
    
    @GET @Produces("text/plain")
    public String getIt() {
        JSONConfiguration jsonConfig = JSONConfiguration.natural().build();
        return jsonConfig.toString();
    }

    @GET @Path("bean") @Produces("application/json")
    public MyBean getJson() {
        return new MyBean();
    }

}
