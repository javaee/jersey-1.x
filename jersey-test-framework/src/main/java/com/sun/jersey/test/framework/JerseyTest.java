package com.sun.jersey.test.framework;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import java.net.URI;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;
import org.junit.After;
import org.junit.Before;

/**
 * The JerseyTest class provides the infrastructure to initialise, instantiate and start
 * a test container, and deploy and/or run tests on the container.
 * The application test classes just have to extend this class, and call one of its
 * constructors which takes care of instantiating the test environment.
 * The JerseyTest class is built using the JUnit 4.x framework.
 * <p>Currently the framework provides support for the following container types:
 * <ul>
 *  <li>Lightweight Grizzly</li>
 *  <li>In Memory Container</li>
 *  <li>HTTPServer</li>
 *  <li>EmbeddedGlassFish</li>
 *  <li>Grizzly Web Container</li>
 *  <li>External Container*</li>
 * </ul>
 * <p>
 * &nbsp;&nbsp;&nbsp;&nbsp;Note: Currently the framework doesn't take care of starting
 * the external container types, but it does allow running tests against an external
 * container like GlassFish or Tomcat, if the application is deployed (explicitly) in the
 * container.
 * @author Paul.Sandoz@Sun.COM, Srinivas.Bhimisetty@Sun.COM
 */
public class JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(JerseyTest.class.getName());

    /**
     * Holds the default test container factory class to be used for running the
     * tests.
     */
    private static Class<? extends TestContainerFactory> defaultTestContainerFactoryClass;

    /**
     * The test container factory which creates an instance of the test container
     * on which the tests would be run.
     */
    private TestContainerFactory testContainerFactory;

    /**
     * The test container on which the tests would be run.
     */
    private final TestContainer tc;

    /**
     * Client instance for creating {@link WebResource} instances and configuring
     * the properties of connections and requests.
     */
    private final Client client;

    /**
     * The no argument constructor.
     * The test class has to provide an implementaion for the {@link #configure()} method,
     * in order to use this variant of the constructor.
     */
    public JerseyTest() {
        AppDescriptor ad = configure();
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * This variant of the constructor takes as parameter an instance of the test
     * container factory. The test class has to provide an implementation of the
     * {@link #configure()} method.
     * @param testContainerFactory
     */
    public JerseyTest(TestContainerFactory testContainerFactory) {
        setTestContainerFactory(testContainerFactory);
        AppDescriptor ad = configure();
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * The test class has to provide an implementation for this method.
     * It is used to build an instance of {@link AppDescriptor} which describes
     * the class of containers with which the tests would be run.
     * @return An instance of {@link AppDescriptor}
     */
    protected AppDescriptor configure() {
        throw new UnsupportedOperationException(
                "The configure method must be implemented by the extending class");
    }

    /**
     * This variant of the constructor takes an <link>AppDescriptor</link> instance
     * as argument and creates an instance of the test container.
     * @param An instance of {@link AppDescriptor}
     */
    public JerseyTest(AppDescriptor ad) {
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * This variant of the constructor takes as argument, an array or a comma separated
     * list of package names which contain resource classes. It builds an instance of
     * {@link WebAppDescriptor} and passes it to the
     * {@link #JerseyTest(com.sun.jersey.test.framework.AppDescriptor)} constructor.
     * @param A string containing the fully qualified root resource package name or
     * an array of fully qualified package names delimited by a semi-colon.
     */
    public JerseyTest(String... packages) {
        this(new WebAppDescriptor.Builder(packages).build());
    }

    /**
     * Sets the test container factory to the passed {@link TestContainerFactory}
     * instance.
     * @param An instance of {@link TestContainerFactory}.
     */
    protected void setTestContainerFactory(TestContainerFactory testContainerFactory) {
        this.testContainerFactory = testContainerFactory;
    }

    /**
     * Returns the test container factory instance.
     * <p>When overridden by a test class it sets the default test container factory
     * for the application.
     * @return An instance of {@link TestContainerFactory}
     */
    protected TestContainerFactory getTestContainerFactory() {
        if (testContainerFactory == null)
            testContainerFactory = getDefaultTestContainerFactory();

        return testContainerFactory;
    }

    /**
     * Creates an instance of {@link WebResource} pointing to the application's base
     * URI.
     * @return An instance of {@link WebResource}
     */
    public WebResource resource() {
        return client.resource(tc.getBaseUri());
    }

    /**
     * Returns an instance of {@link Client}.
     * @return An instance of {@link Client}
     */
    public Client client() {
        return client;
    }

    /**
     * This {@code @Before} annotated method calls the {@link TestContainer}
     * instance's {@code start()} method. The method gets called before executing
     * each test method.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        tc.start();
    }

    /**
     * This {@code @After} annotated method calls the {@link TestContainer} instance's
     * {@code stop()} method. The method gets called after executing each test method.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        tc.stop();
    }

    /**
     * Creates an instance of {@link TestContainer} from the passed instances of
     * {@link AppDescriptor} and {@link TestContainerFactory}. If the test container
     * factory doesn't support the application descriptor, a {@link TestContainerException}
     * is thrown.
     * @param An instance of {@link AppDescriptor}
     * @param An instance of {@link TestContainerFactory}
     * @return An instance of {@link TestContainer}
     */
    private static TestContainer getContainer(AppDescriptor ad, TestContainerFactory tcf) {
        if (ad == null)
            throw new IllegalArgumentException("The application descriptor cannot be null");

        Class<? extends AppDescriptor> adType = tcf.supports();
        if (adType == LowLevelAppDescriptor.class &&
                ad.getClass() == WebAppDescriptor.class) {
            ad = LowLevelAppDescriptor.transform((WebAppDescriptor)ad);
        } else if (adType != ad.getClass()) {
            throw new TestContainerException("The applcation descriptor type, " +
                    ad.getClass() +
                    ", is not supported by the test container factory, " + tcf);
        }

        return tcf.create(getBaseURI(), ad);
    }

    /**
     * Creates an instance of the default test container factory.
     * 
     * @return An instance of {@link TestContainerFactory}
     */
    private static TestContainerFactory getDefaultTestContainerFactory() {
        if (defaultTestContainerFactoryClass == null) {
            defaultTestContainerFactoryClass = getDefaultTestContainerFactoryClass();
        }

        try {
            return defaultTestContainerFactoryClass.newInstance();
        } catch (Exception ex) {
            throw new TestContainerException(
                    "The default test container factory, " +
                    defaultTestContainerFactoryClass +
                    ", could not be instantiated", ex);
        }
    }

    // Refer to the class name rather than the class so we do not introduce
    // a required runtime dependency for those that do not want to utilize
    // Grizzly.
    private static final String DEFAULT_TEST_CONTAINER_FACTORY_CLASS_NAME =
            "com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory";

    /**
     * Returns the default test container factopry class.
     * The default test container factory class could be specified using the System Property -
     *  {@literal test.containerFactory}.
     * <p> This property {@literal test.containerFactory} has to be assigned to the
     * fully qualified class name of the test container factory class.
     * <p> The Jersey Test Framework provides the following factory class
     * implementations for the various test container types:
     * <ul>
     *  <li>{@code com.sun.jersey.test.framework.spi.container.http.HTTPContainerFactory} - HTTPServer</li>
     *  <li>{@code com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory} - Light Weight Grizzly Server</li>
     *  <li>{@code com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory} - Grizzly Web Server</li>
     *  <li>{@code com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory} - In-Memory Test Server</li>
     *  <li>{@code com.sun.jersey.test.framework.spi.container.embedded.glassfish.EmbeddedGlassFishTestContainerFactory} - Embedded GlassFish</li>
     *  <li>{@code com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory} - External Server</li>
     * </ul>
     * <p> This also allows users to plugin their own implementations of test container factories.
     * @return A class implementing the {@link TestContainerFactory} interface.
     */
    private static Class<? extends TestContainerFactory> getDefaultTestContainerFactoryClass() {
        String tcfClassName = System.getProperty("test.containerFactory",
                DEFAULT_TEST_CONTAINER_FACTORY_CLASS_NAME);

        try {
            return (Class<? extends TestContainerFactory>) Class.forName(tcfClassName);
        } catch (ClassNotFoundException ex) {
            throw new TestContainerException(
                    "The default test container factory class name, " +
                    tcfClassName +
                    ", cannot be loaded", ex);
        } catch (ClassCastException ex) {
            throw new TestContainerException(
                    "The default test container factory class, " +
                    tcfClassName +
                    ", is not an instance of TestContainerFactory", ex);
        }
    }

    //////////////////////////////
    //TODO: Check that this method isn't being called anywhere and DELETE it
    /////////////////////////////
    /**
     * Sets the default test container for running the tests.
     * This needs to be called in a @BeforeClass annotated method of the test class.
     * It is advised to reset the default the test container to null in a @AfterClass
     * annotated method of the test class.
     * <p> One of <strong>HTTPServer</strong>, <strong>Grizzly</strong>,
     * <strong>GrizzlyWeb</strong>, <strong>InMemory</strong>, <strong>EmbeddedGF</strong>
     * could be used as the default test containers.
     * @param testContainerType
     */
    /*
    protected static void setDefaultTestContainerFactory(
            Class<? extends TestContainerFactory> rcf) {
        defaultTestContainerFactoryClass = rcf;
    }
     */

    /**
     * Creates an instance of {@link Client}.
     * @param An instance of {@link TestContainer}
     * @param An instance of {@link AppDescriptor}
     * @return A Client instance.
     */
    private static Client getClient(TestContainer tc, AppDescriptor ad) {
        Client c = tc.getClient();

        if (c != null) {
            return c;
        } else {
            c = Client.create(ad.getClientConfig());
        }

        //check if logging is required
        boolean enableLogging = (System.getProperty("enableLogging") != null)
                ? true
                : false;
        
        if (enableLogging) {
                c.addFilter(new LoggingFilter());
        }

        return c;
    }

    /**
     * Returns the base URI of the application.
     * @return The base URI of the application
     */
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/")
                .port(getPort(9998)).build();
    }

    /**
     * Returns the port to be used in the base URI.
     * @param defaultPort
     * @return The HTTP port of the URI
     */
    private static int getPort(int defaultPort) {
        String port = System.getProperty("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }
}