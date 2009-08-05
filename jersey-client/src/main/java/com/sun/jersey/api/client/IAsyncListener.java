package com.sun.jersey.api.client;

/**
 * A listener to be implemented by clients that wish to receive callback
 * notification of the completion of requests invoked asynchronously.
 * <p>
 * Developers may wish to extend from the class {@link AsyncListener} rather
 * than implement this interface directly.
 *
 * @see AsyncListener.
 * @param <T> the type of the response.
 */
public interface IAsyncListener<T> {

    /**
     * Get the class of the instance to receive for
     * {@link #onResponse(java.lang.Object)  }.
     * 
     * @return the class of the response.
     */
    Class<T> getType();

    /**
     * Get the generic type declaring the Java type of the instance to
     * receive for {@link #onResponse(java.lang.Object)  }.
     * 
     * @return the generic type of the response. If null then the method
     *         {@link #getType() } must not return null. Otherwise, if not null,
     *         the type information declared by the generic type takes
     *         precedence over the value returned by {@link #getType() }.
     */
    GenericType<T> getGenericType();

    /**
     * Called when an error occurs.
     *
     * @param t the exeception indicating the error. May be an instance of
     *        {@link UniformInterfaceException} if the status of the HTTP 
     *        response is greater than or equal to 300 and <code>T</code> is 
     *        not the type {@link ClientResponse}.
     */
    void onError(Throwable t);

    /**
     * Called when a response is received.
     * 
     * @param t the response.
     */
    void onResponse(T t);
}
