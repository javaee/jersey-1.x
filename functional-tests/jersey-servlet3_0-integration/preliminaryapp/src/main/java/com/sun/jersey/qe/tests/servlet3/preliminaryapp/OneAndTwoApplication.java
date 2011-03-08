package com.sun.jersey.qe.tests.servlet3.preliminaryapp;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Application;

public class OneAndTwoApplication extends Application {
    // TODO
    // This is necessary because the 311 jar in GF takes precedence,
    // regardles of the class loading delegation property, for classes
    // in the "javax.*" package.
    // One the latest 311 jar is included in GF this will not be required.
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }
}
