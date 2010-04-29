package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.client.view.annotation.Status;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;

public class Update<T> {

    private final Class<T> tClass;

    ViewResource updateResource;

    public Update(Class<T> tClass, ViewResource updateResource) {
        this.tClass = tClass;
        this.updateResource = updateResource;
    }

    @PUT
    @Status(200)
    @Consumes("*/*")
    public void build(@Context ClientResponse cr) {
    }

    public T view() {
        return updateResource.get(tClass);
    }
}
