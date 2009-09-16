package com.sun.jersey.samples.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.samples.guice.resources.PerRequestResource;

/**
 *
 * @author paulsandoz
 */
public class GuiceServletConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ServletModule() {

            @Override
            protected void configureServlets() {
                // Bind classes
                bind(PerRequestResource.class);
                
                serve("/*").with(GuiceContainer.class);
            }
        });
    }
}