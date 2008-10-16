package com.sun.jersey.samples.groovy

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Example Groovy class.
 */
@Path("groovy")
class GroovyResource {
    @GET def show() {
        "groovy"
    }
}
