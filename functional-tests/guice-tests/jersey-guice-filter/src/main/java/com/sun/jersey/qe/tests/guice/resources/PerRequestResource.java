package com.sun.jersey.qe.tests.guice.resources;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("bound/perrequest")
@RequestScoped
public class PerRequestResource {

    @Context UriInfo ui;

    @QueryParam("x") String x;

    private final SingletonComponent sc;

    @Inject
    public PerRequestResource(SingletonComponent sc) {
        this.sc = sc;
    }

    @GET
    public String get() {
        return this + " " + ui.getPath() + " " + x + " " + sc;
    }
}
