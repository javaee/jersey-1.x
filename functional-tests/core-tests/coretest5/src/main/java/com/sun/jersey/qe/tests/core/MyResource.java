package com.sun.jersey.qe.tests.core;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("myresource")
public class MyResource {

    @GET
    @Produces("text/plain")
    public String getIt() {
        return "Hi There!";
    }

}
