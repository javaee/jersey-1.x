package com.sun.jersey.samples.groovy

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Example Groovy class.
 */
@Path("groovy")
class GroovyResource {
    @Produces(["text/plain"]) @GET def show() {
        "groovy"
    }
}