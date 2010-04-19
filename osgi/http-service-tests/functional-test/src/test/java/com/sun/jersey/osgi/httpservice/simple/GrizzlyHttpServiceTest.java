package com.sun.jersey.osgi.httpservice.simple;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;


import org.junit.runner.RunWith;
import org.junit.Test;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;


@RunWith(JUnit4TestRunner.class)
public class GrizzlyHttpServiceTest extends AbstractHttpServiceTest {


    @Override
    public Option[] httpServiceProviderConfiguration() {
        Option[] options = options(
                mavenBundle("com.sun.grizzly.osgi", "grizzly-httpservice-bundle", "1.9.19-beta1"));
                 
        return options;
    }

    @Test
    public void testJerseyServlet() throws Exception {
        super.defaultJerseyServletTestMethod();
    }

    @Test
    public void testNonJerseyServlet() throws Exception {
        super.defaultNonJerseyServletTestMethod();
    }

}

