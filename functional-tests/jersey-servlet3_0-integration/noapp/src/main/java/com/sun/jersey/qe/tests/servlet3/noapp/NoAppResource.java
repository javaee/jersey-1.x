package com.sun.jersey.qe.tests.servlet3.noapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class NoAppResource {
    
    @GET 
    @Produces("text/plain")
    public String get() {
        return "GET";
    }
}
