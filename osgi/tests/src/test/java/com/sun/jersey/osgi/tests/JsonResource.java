package com.sun.jersey.osgi.tests;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;


@Path("json")
public class JsonResource {

    @XmlRootElement
    public static class NameBean {
        public String name = "Harrison";
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public NameBean getBean() {

        NameBean result = new NameBean();
        result.name = "Jim";

        return result;
    }
}
