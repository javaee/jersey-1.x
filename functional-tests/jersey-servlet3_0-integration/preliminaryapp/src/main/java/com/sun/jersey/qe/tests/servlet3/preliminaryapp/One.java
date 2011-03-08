package com.sun.jersey.qe.tests.servlet3.preliminaryapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/one")
public class One {
    
    @GET 
    @Produces("text/plain")
    public String get() {
        return "ONE";
    }
}
