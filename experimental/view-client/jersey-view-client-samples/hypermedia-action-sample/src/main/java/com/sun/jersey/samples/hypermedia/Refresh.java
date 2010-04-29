package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.client.view.annotation.Status;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;

public class Refresh<T> {

    private final Class<T> tClass;

    private ViewResource refreshResource;

    public Refresh(Class<T> tClass) {
        this.tClass = tClass;
    }

    @POST
    @PUT
    @Consumes("*/*")
    public void build(@Context ClientResponse cr) {
        refreshResource = cr.getLinks().viewResource("refresh");
    }

    public T view() {
        return refreshResource.get(tClass);
    }
}
