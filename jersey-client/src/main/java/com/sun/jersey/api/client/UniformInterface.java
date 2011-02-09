/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.api.client;

import com.sun.jersey.api.client.config.ClientConfig;

/**
 * A uniform interface for invoking HTTP requests.
 * <p>
 * Any Java type for a response entity, that is supported by the client
 * configuration of the client, may be declared using
 * <code>Class&lt;T&gt;</code> where <code>T</code> is the Java type, or
 * using {@link GenericType} where the generic parameter is the Java type.
 * <p>
 * Any Java type instance for a request entity, that is supported by the client
 * configuration of the client, can be passed. If generic information is
 * required then an instance of {@link javax.ws.rs.core.GenericEntity} may
 * be used.
 * <p>
 * A type of {@link ClientResponse} declared
 * for the response entity may be used to obtain the status, headers and
 * response entity.
 * <p>
 * If any type, other than {@link ClientResponse},
 * is declared and the response status is greater than or equal to 300 then a
 * {@link UniformInterfaceException} exception
 * will be thrown, from which the
 * {@link ClientResponse} instance can be
 * accessed.
 * <p>
 * In the following cases it is necessary to close the response, when response
 * processing has completed, to ensure that underlying resources are
 * correctly released.
 * <p>
 * If a response entity is declared of the type
 * {@link ClientResponse}
 * or of a type that is assignable to {@link java.io.Closeable}
 * (such as {@link java.io.InputStream}) then the response must be either:
 * 1) closed by invoking the method
 * {@link ClientResponse#close() } or
 * {@link java.io.Closeable#close}; or 2) all bytes of response entity must be
 * read.
 * <p>
 * If a {@link UniformInterfaceException} is
 * thrown then by default the response entity is automatically buffered and
 * the underlying resources are correctly released. See the following property
 * for more details:
 * {@link ClientConfig#PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION}.
 * 
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.client
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
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T options(Class<T> c) throws UniformInterfaceException;
    
    
    /**
     * Invoke the OPTIONS method.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T options(GenericType<T> gt) throws UniformInterfaceException;
    
    /**
     * Invoke the GET method.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T get(Class<T> c) throws UniformInterfaceException;
            
    /**
     * Invoke the GET method.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T get(GenericType<T> gt) throws UniformInterfaceException;

    
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
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T put(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the PUT method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T put(GenericType<T> gt) throws UniformInterfaceException;
    
    /**
     * Invoke the PUT method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T put(Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;
    
    /**
     * Invoke the PUT method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T put(GenericType<T> gt, Object requestEntity) 
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
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T post(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the POST method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T post(GenericType<T> gt) throws UniformInterfaceException;
    
    /**
     * Invoke the POST method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T post(Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;
                
    /**
     * Invoke the POST method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T post(GenericType<T> gt, Object requestEntity)
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
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T delete(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T delete(GenericType<T> gt) throws UniformInterfaceException;
    
    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T delete(Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;

    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T delete(GenericType<T> gt, Object requestEntity)
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
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T method(String method, Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke a HTTP method with no request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T method(String method, GenericType<T> gt) throws UniformInterfaceException;
    
    /**
     * Invoke a HTTP method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>c</code> is not the type
     *         {@link ClientResponse}.
     */
    <T> T method(String method, Class<T> c, Object requestEntity) 
            throws UniformInterfaceException;    
    
    /**
     * Invoke a HTTP method with a request entity that returns a response.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     * @throws UniformInterfaceException if the status of the HTTP response is 
     *         greater than or equal to 300 and <code>gt</code> does not
     *         represent the type {@link ClientResponse}.
     */
    <T> T method(String method, GenericType<T> gt, Object requestEntity) 
            throws UniformInterfaceException;    
}