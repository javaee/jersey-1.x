package com.sun.jersey.api.client;

/**
 * A listener to be implemented by clients that wish to receive callback
 * notification of the completion of requests invoked asynchronously.
 * <p>
 * This listener is a helper class providing implementions for the methods
 * {@link IAsyncListener#getType() } and {@link IAsyncListener#getGenericType() }.
 * <p>
 * Instances of this class may be passed to appropriate methods on
 * {@link AsyncWebResource} (or more specifically methods on 
 * {@link AsyncUniformInterface}). For example,
 * <blockquote><pre>
 *     AsyncWebResource r = ..
 *     Future&lt;?&gt; f = r.get(new AsyncListener&lt;String&gt;(String.class) {
 *         public void onError(Throwable t) {
 *             // Do error processing
 *             if (t instanceof UniformInterfaceException) {
 *                 // Request/response error
 *             } else {
 *                 // Error making request e.g. timeout
 *             }
 *         }
 *
 *         public void onResponse(String t) {
 *             // Process response entity as a String instance
 *         }
 *     });
 * </pre></blockquote>
 *
 * @param <T> the type of the response.
 */
public abstract class AsyncListener<T> implements IAsyncListener<T> {

    private final Class<T> type;

    private final GenericType<T> genericType;

    // TODO
//    public AsyncListener() {
//        // determine type or genericType from reflection
//    }

    /**
     * Construct a new listener defining the class of the response to receive.
     *
     * @param type the class of the response.
     */
    public AsyncListener(Class<T> type) {
        this.type = type;
        this.genericType = null;
    }

    /**
     * Construct a new listener defining the generic type of the response to
     * receive.
     *
     * @param genericType the generic type of the response.
     */
    public AsyncListener(GenericType<T> genericType) {
        this.type = genericType.getRawClass();
        this.genericType = genericType;
    }

    public Class<T> getType() {
        return type;
    }

    public GenericType<T> getGenericType() {
        return genericType;
    }
}