package com.sun.jersey.qe.tests.servlet3.annotatedappwithservletmapping;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/two")
public class Two {
    
    @GET 
    @Produces("text/plain")
    public String get() {
        return "TWO";
    }
}
