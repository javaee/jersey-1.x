/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.jersey.api.client;

/**
 * A uniform interface for invoking HTTP requests.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface UniformInterface {
    
    /**
     * Invoke the HEAD method.
     * 
     * @return the HTTP response.
     */
    ClientResponse head();
    
    
    /**
     * Invoke the OPTIONS method.
     * 
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T options(Class<T> c) throws UniformInterfaceException;
    
    
    /**
     * Invoke the GET method.
     * 
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T get(Class<T> c) throws UniformInterfaceException;
            
    
    /**
     * Invoke the PUT method with no request entity or response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void put() throws UniformInterfaceException;
    
    /**
     * Invoke the PUT method with a request entity but no response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @param requestEntity the request entity.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void put(Object requestEntity) throws UniformInterfaceException;

    /**
     * Invoke the PUT method with no request entity that returns a response.
     * 
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T put(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the PUT method with a request entity that returns a response.
     * 
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T put(Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;
    
    
    /**
     * Invoke the POST method with no request entity or response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void post() throws UniformInterfaceException;
    
    /**
     * Invoke the POST method with a request entity but no response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @param requestEntity the request entity.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void post(Object requestEntity) throws UniformInterfaceException;
    
    /**
     * Invoke the POST method with no request entity that returns a response.
     * 
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T post(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the POST method with a request entity that returns a response.
     * 
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T post(Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;
            
    
    /**
     * Invoke the DELETE method with no request entity or response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void delete() throws UniformInterfaceException;
    
    /**
     * Invoke the DELETE method with a request entity but no response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @param requestEntity the request entity.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void delete(Object requestEntity) throws UniformInterfaceException;
    
    /**
     * Invoke the DELETE method with no request entity that returns a response.
     * 
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T delete(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * 
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T delete(Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;

    
    /**
     * Invoke a HTTP method with no request entity or response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @param method the HTTP method.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void method(String method) throws UniformInterfaceException;
    
    /**
     * Invoke a HTTP method with a request entity but no response.
     * <p>
     * If the status code is less than 300 and a representation is present
     * then that representation is ignored.
     * 
     * @param method the HTTP method.
     * @param requestEntity the request entity.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300.
     */
    void method(String method, Object requestEntity) 
            throws UniformInterfaceException;
    
    /**
     * Invoke a HTTP method with no request entity that returns a response.
     * 
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T method(String method, Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke a HTTP method with a request entity that returns a response.
     * 
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c<code> is not the type 
     *         {@link ClientResponse}.
     */
    <T> T method(String method, Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;    
}