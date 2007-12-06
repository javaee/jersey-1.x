package com.sun.ws.rest.api.container.grizzly;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import com.sun.ws.rest.api.container.ContainerFactory;
import java.net.URI;

/**
 * Factory for creating and starting Grizzly {@link SelectorThread} instances.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class GrizzlyServerFactory {
    
    private GrizzlyServerFactory() {}
    
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * To avoid potential race conditions with the returned 
     * {@link SelectorThread} instance it is recommended to sleep for a
     * period of time after this method has been invoked to ensure the 
     * {@link SelectorThread} has had enough time to intialize to the correct
     * state.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Adapter)} method for creating
     * an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @return the select thread, with the endpoint started
     */
    public static SelectorThread create(String u) {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u));
    }
    
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * To avoid potential race conditions with the returned 
     * {@link SelectorThread} instance it is recommended to sleep for a
     * period of time after this method has been invoked to ensure the 
     * {@link SelectorThread} has had enough time to intialize to the correct
     * state.
     * <p>
     * This implementation defers to the 
     * {@link ContainerFactory#createContainer(Adapter)} method for creating
     * an Adapter that manages the root resources.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @return the select thread, with the endpoint started
     */
    public static SelectorThread create(URI u) {
        return create(u, ContainerFactory.createContainer(Adapter.class));
    }
        
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * To avoid potential race conditions with the returned 
     * {@link SelectorThread} instance it is recommended to sleep for a
     * period of time after this method has been invoked to ensure the 
     * {@link SelectorThread} has had enough time to intialize to the correct
     * state.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param adapter the Adapter
     * @return the select thread, with the endpoint started
     */
    public static SelectorThread create(String u, Adapter adapter) {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u));
    }
    
    /**
     * Create a {@link SelectorThread} that registers an {@link Adapter} that 
     * in turn manages all root resource classes found by searching the classes
     * referenced in the java classath.
     * <p>
     * To avoid potential race conditions with the returned 
     * {@link SelectorThread} instance it is recommended to sleep for a
     * period of time after this method has been invoked to ensure the 
     * {@link SelectorThread} has had enough time to intialize to the correct
     * state.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *        equal to "http". The URI user information and host
     *        are ignored If the URI port is not present then port 80 will be 
     *        used. The URI path, query and fragment components are ignored.
     * @param adapter the Adapter
     * @return the select thread, with the endpoint started
     */
    public static SelectorThread create(URI u, Adapter adapter) {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");
            
        final String scheme = u.getScheme();
        if (!scheme.equalsIgnoreCase("http"))
            throw new IllegalArgumentException("The URI scheme, of the URI " + u + 
                    ", must be equal (ignoring case) to 'http'");            
        
        final SelectorThread selectorThread = new SelectorThread();

        selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
        
        final int port = (u.getPort() == -1) ? 80 : u.getPort();            
        selectorThread.setPort(port);

        selectorThread.setAdapter(adapter);
        
        selectorThread.listen();
        return selectorThread;
    }    
}