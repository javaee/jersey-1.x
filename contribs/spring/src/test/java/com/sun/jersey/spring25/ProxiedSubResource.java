package com.sun.jersey.spring25;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class ProxiedSubResource {

    @Context
    private UriInfo _uriInfo;

    @GET
    @Produces(value = "text/plain")
    public String getRequestUri() {
        // if the uriInfo is not injected - of course - this produces an NPE
        return _uriInfo.getRequestUri().toString();
    }
}
