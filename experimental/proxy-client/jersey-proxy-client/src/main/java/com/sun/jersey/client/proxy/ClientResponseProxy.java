package com.sun.jersey.client.proxy;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Martin Matula (martin.matula at oracle.com)
 */
public class ClientResponseProxy extends Response {
    private final ClientResponse cr;

    ClientResponseProxy(ClientResponse cr) {
        this.cr = cr;
    }

    @Override
    public Object getEntity() {
        return cr.getEntityInputStream();
    }

    @Override
    public int getStatus() {
        return cr.getStatus();
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        MultivaluedMap headers = cr.getHeaders();
        return (MultivaluedMap<String, Object>) headers;
    }

    public ClientResponse getClientResponse() {
        return cr;
    }
}
