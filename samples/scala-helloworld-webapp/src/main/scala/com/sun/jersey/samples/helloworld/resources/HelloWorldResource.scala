package com.sun.jersey.samples.helloworld.resources

import javax.ws.rs.{GET, Path, Produces}

@Path("helloworld")
class HelloWorldResource {

    @Produces(Array("text/plain"))
    @GET
    def get() = "Hello World"
}