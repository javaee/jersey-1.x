package com.sun.jersey.osgi.httpservice.simple;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@RunWith(JUnit4TestRunner.class)
public class JettyHttpServiceTest extends AbstractHttpServiceTest {


    @Override
    public Option[] httpServiceProviderConfiguration() {
        Option[] options = options(
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jetty-bundle", "6.1.14_2")
                , mavenBundle("org.ops4j.pax.web", "pax-web-api", "0.7.1")
                , mavenBundle("org.ops4j.pax.web", "pax-web-spi", "0.7.1")
                , mavenBundle("org.ops4j.pax.web", "pax-web-runtime", "0.7.1")
                , mavenBundle("org.ops4j.pax.web", "pax-web-jetty", "0.7.1")
                , felix());

        return options;
    }

    @Test
    public void testJerseyServlet() throws Exception {
        super.defaultJerseyServletTest();
    }

    @Test
    public void testNonJerseyServlet() throws Exception {
        super.defaultNonJerseyServletTest();
    }
}

