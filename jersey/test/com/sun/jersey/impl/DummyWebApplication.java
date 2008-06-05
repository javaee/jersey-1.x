/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.impl;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentProvider;
import javax.ws.rs.ext.MessageBodyWorkers;

/**
 *
 * @author ps23762
 */
public class DummyWebApplication implements WebApplication {

    public void initiate(ResourceConfig resourceConfig) throws IllegalArgumentException, ContainerException {
    }

    public void initiate(ResourceConfig resourceConfig, ComponentProvider provider) throws IllegalArgumentException, ContainerException {
    }

    @Override
    public WebApplication clone() {
        return null;
    }

    public MessageBodyWorkers getMessageBodyWorkers() {
        return null;
    }

    public ComponentProvider getComponentProvider() {
        return null;
    }

    public ComponentProvider getResourceComponentProvider() {
        return null;
    }

    public void addInjectable(InjectableProvider<?, ?> ip) {
    }

    public HttpContext getThreadLocalHttpContext() {
        return null;
    }

    public void handleRequest(ContainerRequest request, ContainerResponse response) throws ContainerException {
    }
}