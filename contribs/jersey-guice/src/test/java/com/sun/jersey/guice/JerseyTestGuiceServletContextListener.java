/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

/**
 *
 * @author paulsandoz
 */
public abstract class JerseyTestGuiceServletContextListener extends GuiceServletContextListener {

    private final ServletModule module;

    public JerseyTestGuiceServletContextListener() {
        this.module = configure();
    }

    protected abstract ServletModule configure();

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(module);
    }
}
