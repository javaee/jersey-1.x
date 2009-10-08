package com.sun.jersey.test.framework;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.embedded.glassfish.EmbeddedGlassFishTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.http.HTTPContainerFactory;
import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.junit.After;
import org.junit.Before;

/**
 * An abstract JUnit 4.x-based unit test class for testing JAX-RS and 
 * Jersey-based applications.
 * <p>
 * At construction this class will obtain a test container factory, of type
 * {@link TestContainerFactory}, and use that to obtain a configured test
 * container, of type {@link TestContainer}.
 * <p>
 * Before a test method, in an extending class, is run the 
 * {@link TestContainer#start() } method is invoked. After the test method has
 * run the {@link TestContainer#stop() } method is invoked.
 * The test method can invoke the {@link #resource() } to obtain a
 * {@link WebResource} from which requests may be sent to and responses recieved
 * from the Web application under test.
 * <p>
 * If a test container factory is not explictly declared using the appropriate
 * constructor (see {@link #JerseyTest(TestContainerFactory) }) then a default
 * test container factory will be obtained as follows.
 * If the system property {@literal test.containerFactory} is set and the
 * value is a fully qualified class name of a class that extends from
 * {@link TestContainerFactory} then the default test container factory will
 * be an instance of that class. The exception {@link TestContainerException}
 * will be thrown if the class cannot be loaded or instantiated.
 * If the system property {@literal test.containerFactory} is not set then
 * the default test container factory will be an instance of 
 * {@link com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory}.
 * The exception {@link TestContainerException} will be thrown if this class 
 * cannot be loaded or instantiated.
 * <p>
 * The test container is configured from an application descriptor, of type
 * {@link AppDescriptor}. The exception {@link TestContainerException}
 * will be thrown if the test container cannot support the application 
 * descriptor.
 * An application descriptor is built from an application descriptor builder.
 * Two application descriptor builders are provided:
 * <ol>
 *  <li>A low-level builder, of type {@link LowLevelAppDescriptor.Builder},
 *      compatible with low-level test containers that do not support Servlets.</li>
 *  <li>A web-based builder, of type {@link WebAppDescriptor.Builder},
 *      compatible with web-based test containers that support Servlets.</li>
 * </ol>
 * An application descriptor of type {@link WebAppDescriptor} may be
 * transformed to an application descriptor of type {@link LowLevelAppDescriptor}
 * if the state of the former is compatible with a low-level description.
 * <p>
 * The following low-level test container factories are provided:
 * <ul>
 *  <li>{@link GrizzlyTestContainerFactory} for testing with the low-level 
 *      Grizzly HTTP container.</li>
 *  <li>{@link HTTPContainerFactory} for testing with the Light Weight HTTP
 *      server distributed with Java SE 6.</li>
 *  <li>{@link InMemoryTestContainerFactory} for testing in memory without
 *      using underlying HTTP client and server side functionality
 *      to send requests and receive responses.</li>
 * </ul>
 * The following Web-based test container factories are provided:
 * <ul>
 *  <li>{@link GrizzlyWebTestContainerFactory} for testing with the Grizzly
 *      Web container and Servlet support.</li>
 *  <li>{@link EmbeddedGlassFishTestContainerFactory} for testing with
 *      embedded GlassFish.</li>
 *  <li>{@link ExternalTestContainerFactory} for testing when the Web
 *      application is independently deployed in a separate JVM to that of the
 *      tests. For example, the application may be deployed to the
 *      Glassfish v2 or v3 application server.</li>
 * </ul>
 * 
 * @author Paul.Sandoz@Sun.COM, Srinivas.Bhimisetty@Sun.COM
 */
public abstract class JerseyTest {

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
     * An extending class must implement the {@link #configure()} method to 
     * provide an application descriptor.
     *
     * @throws TestContainerException if the default test container factory
     *         cannot be obtained, or the application descriptor is not
     *         supported by the test container factory.
     */
    public JerseyTest() throws TestContainerException {
        AppDescriptor ad = configure();
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * Contruct a new instance with a test container factory.
     * <p>
     * An extending class must implement the {@link #configure()} method to 
     * provide an application descriptor.
     *
     * @param testContainerFactory the test container factory to use for testing.
     * @throws TestContainerException if the application descriptor is not
     *         supported by the test container factory.
     */
    public JerseyTest(TestContainerFactory testContainerFactory) {
        setTestContainerFactory(testContainerFactory);
        AppDescriptor ad = configure();
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * Return an application descriptor that defines how the test container
     * is configured.
     * <p>
     * If a constructor is utilized that does not supply an application
     * descriptor then this method must be overriden to return an application
     * descriptor, otherwise an {@link UnsupportedOperationException} exception
     * will be thrown.
     * <p>
     * If a constructor is utilized that does supply an application descriptor
     * then this method does not require to be overridden and will not be
     * invoked.
     *
     * @return the application descriptor.
     */
    protected AppDescriptor configure() {
        throw new UnsupportedOperationException(
                "The configure method must be implemented by the extending class");
    }

    /**
     * Construct a new instance with an application descriptor that defines
     * how the test container is configured.
     *
     * @param ad an application descriptor describing how to configure the 
     *        test container.
     * @throws TestContainerException if the default test container factory
     *         cannot be obtained, or the application descriptor is not
     *         supported by the test container factory.
     */
    public JerseyTest(AppDescriptor ad) throws TestContainerException {
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * Construct a new instance with an array or a colon separated
     * list of package names which contain resource and provider classes.
     * <p>
     * This contructor builds an instance of {@link WebAppDescriptor} passing
     * the package names to the constructor.
     * 
     * @param packages array or a colon separated list of package names which
     *        contain resource and provider classes.
     * @throws TestContainerException if the default test container factory
     *         cannot be obtained, or the built application descriptor is not
     *         supported by the test container factory.
     */
    public JerseyTest(String... packages) throws TestContainerException {
        this(new WebAppDescriptor.Builder(packages).build());
    }

    /**
     * Sets the test container factory to to be used for testing.
     * 
     * @param testContainerFactory the test container factory to to be used for
     *        testing.
     */
    protected void setTestContainerFactory(TestContainerFactory testContainerFactory) {
        this.testContainerFactory = testContainerFactory;
    }

    /**
     * Get the test container factory.
     * <p>
     * If the test container factory has not been explicit set with 
     * {@link #setTestContainerFactory(TestContainerFactory) } then
     * the default test container factory will be obtained.
     * <p>
     * If the system property {@literal test.containerFactory} is set and the
     * value is a fully qualified class name of a class that extends from
     * {@link TestContainerFactory} then the default test container factory will
     * be an instance of that class. The exception {@link TestContainerException}
     * will be thrown if the class cannot be loaded or instantiated.
     * If the system property {@literal test.containerFactory} is not set then
     * the default test container factory will be an instance of
     * {@link com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory}.
     * The exception {@link TestContainerException} will be thrown if this class
     * cannot be loaded or instantiated.
     *
     *
     * @return the test container factory.
     * @throws TestContainerException if the default test container factory
     *         cannot be obtained.
     */
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        if (testContainerFactory == null)
            testContainerFactory = getDefaultTestContainerFactory();

        return testContainerFactory;
    }

    /**
     * Create a web resource whose URI refers to the base URI the Web
     * application is deployed at.
     *
     * @return the created web resource
     */
    public WebResource resource() {
        return client.resource(tc.getBaseUri());
    }

    /**
     * Get the client that is configured for this test.
     *
     * @return the configured client.
     */
    public Client client() {
        return client;
    }

    /**
     * Set up the test by invoking {@link TestContainer#start() } on
     * the test container obtained from the test container factory.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        tc.start();
    }

    /**
     * Tear down the test by invoking {@link TestContainer#stop() } on
     * the test container obtained from the test container factory.
     *
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
    private TestContainer getContainer(AppDescriptor ad, TestContainerFactory tcf) {
        if (ad == null)
            throw new IllegalArgumentException("The application descriptor cannot be null");

        Class<? extends AppDescriptor> adType = tcf.supports();
        if (adType == LowLevelAppDescriptor.class &&
                ad.getClass() == WebAppDescriptor.class) {
            ad = LowLevelAppDescriptor.transform((WebAppDescriptor)ad);
        } else if (adType != ad.getClass()) {
            throw new TestContainerException("The application descriptor type, " +
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
    protected URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/")
                .port(getPort(9998)).build();
    }

    /**
     * Returns the port to be used in the base URI.
     * @param defaultPort
     * @return The HTTP port of the URI
     */
    protected int getPort(int defaultPort) {
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