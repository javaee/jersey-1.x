package com.sun.jersey.qe.tests.core;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("myresource")
public class MyResource {

    public MyResource() {
        DefaultResourceConfig defaultResourceConfig = new DefaultResourceConfig();
        defaultResourceConfig.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL,
                Boolean.TRUE);
    }

    @GET
    @Produces("text/plain")
    public String getIt() {
        return "Hi There!";
    }

}
