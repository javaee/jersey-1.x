package com.sun.jersey.qe.tests.servlet3.annotatedapp;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/twoonly")
public class TwoApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(Two.class);
    }
}
