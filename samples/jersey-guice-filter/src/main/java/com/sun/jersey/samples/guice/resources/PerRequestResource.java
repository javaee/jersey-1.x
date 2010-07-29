package com.sun.jersey.samples.guice.resources;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

//Create resource class, @Path("bound/perrequest"), using guice @RequestScoped
@Path("bound/perrequest")
@RequestScoped
public class PerRequestResource {

    //Inject URI info and a query parameter
    @Context UriInfo ui;

    @QueryParam("x") String x;

    private final SingletonComponent sc;

    //Create singleton component and inject into resource at construction
    @Inject
    public PerRequestResource(SingletonComponent sc) {
        this.sc = sc;
    }

    @GET
    public String get() {
        return this + " " + ui.getPath() + " " + x + " " + sc;
    }
}
