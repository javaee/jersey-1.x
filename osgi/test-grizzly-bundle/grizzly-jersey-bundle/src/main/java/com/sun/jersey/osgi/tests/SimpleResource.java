package com.sun.jersey.osgi.tests;

import javax.ws.rs.*;

/**
 * 
 * @author japod
 */
@Path("/simple")
public class SimpleResource {

	@GET @Produces("text/plain")
	public String getMe() {
		return "OK";
	}
	
}
