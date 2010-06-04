/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.guice;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author paulsandoz
 */
public class JerseyTestServletModule extends ServletModule {
    private Set<Class<?>> clazzes = new HashSet<Class<?>>();

    private String path = "/*";

    private Map<String, String> initParams = new HashMap<String, String>();

    private Class<? extends GuiceContainer> servletClass = GuiceContainer.class;
    
    public JerseyTestServletModule servlet(Class<? extends GuiceContainer> servletClass) {
        this.servletClass = servletClass;
        return this;
    }

    public JerseyTestServletModule path(String path) {
        this.path = path;
        return this;
    }

    public JerseyTestServletModule bindClass(Class<?> clazz) {
        clazzes.add(clazz);
        return this;
    }

    public JerseyTestServletModule initParam(String name, String value) {
        initParams.put(name, value);
        return this;
    }

    @Override
    protected void configureServlets() {
        configureBind();
        configureServe();
    }

    protected void configureBind() {
        for (Class<?> clazz : clazzes) {
            bind(clazz);
        }
    }
    
    protected void configureServe() {
        serve(path).with(servletClass, initParams);
    }
}
