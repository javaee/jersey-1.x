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
 * </ul>
 * <p>
 * @author paulsandoz
 */
public class JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(JerseyTest.class.getName());

    private static Class<? extends TestContainerFactory> defaultTestContainerFactoryClass;

    private TestContainerFactory testContainerFactory;

    private final TestContainer tc;

    private final Client client;


    /**
     * The no argument constructor.
     * The test class has to provide an implementaion for the <link>configure()</link> method,
     * in order to use this variant of the constructor.
     */
    public JerseyTest() {
        AppDescriptor ad = configure();
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    public JerseyTest(TestContainerFactory testContainerFactory) {
        setTestContainerFactory(testContainerFactory);
        AppDescriptor ad = configure();
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * The test class has to provide an implementation for this method.
     * It is used to build an instance of <link>AppDescriptor</link> which describes
     * the class of containers with which the tests would be run.
     * @return <link>AppDescriptor</link>
     */
    protected AppDescriptor configure() {
        throw new UnsupportedOperationException(
                "The configure method must be implemented by the extending class");
    }

    /**
     * This variant of the constructor takes an <link>AppDescriptor</link> instance
     * as argument and creates an instance of the test container.
     * @param ad
     */
    public JerseyTest(AppDescriptor ad) {
        this.tc = getContainer(ad, getTestContainerFactory());
        this.client = getClient(tc, ad);
    }

    /**
     * This variant of the constructor takes as argument, an array or a comma separated
     * list of package names which contain resource classes. It builds an instance of
     * <link>WebAppDescriptor</link> and passes it to the <link>JerseyTest(AppDescriptor ad)</link>
     * constructor.
     * @param packages
     */
    public JerseyTest(String... packages) {
        this(new WebAppDescriptor.Builder(packages).build());
    }

    protected void setTestContainerFactory(TestContainerFactory testContainerFactory) {
        this.testContainerFactory = testContainerFactory;
    }

    protected TestContainerFactory getTestContainerFactory() {
        if (testContainerFactory == null)
            testContainerFactory = getDefaultTestContainerFactory();

        return testContainerFactory;
    }

    /**
     * Creates a Web resource pointing to the application's base URI.
     * @return
     */
    public WebResource resource() {
        return client.resource(tc.getBaseUri());
    }

    public Client client() {
        return client;
    }

    /**
     * Starts the test container.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        tc.start();
    }

    /**
     * Stops the test container.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        tc.stop();
    }

    /**
     * Creates a test container instance.
     * @param ad
     * @return
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
     * Creates an instance of the test container factory.
     * @return
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
            "com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory";

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
    protected static void setDefaultTestContainerFactory(
            Class<? extends TestContainerFactory> rcf) {
        defaultTestContainerFactoryClass = rcf;
    }

    /**
     * Creates a <link>Client<link> instance.
     * @param tc
     * @param ad
     * @return
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
     * @return
     */
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/")
                .port(getPort(9998)).build();
    }

    /**
     * Returns the port to be used in the base URI.
     * @param defaultPort
     * @return
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