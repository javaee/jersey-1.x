/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.jersey.api.client.*;
import java.util.concurrent.Future;

/**
 * An asynchronous uniform interface for invoking HTTP requests.
 * 
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
 * A type of {@link ClientResponse} declared for the response entity
 * may be used to obtain the status, headers and response entity. If any other
 * type is declared and the response status is greater than or equal to
 * 300 then a {@link UniformInterfaceException} exception will be thrown, from 
 * which the {@link ClientResponse} instance can be accessed.
 * 
 * @see com.sun.jersey.api.client
 * @author Paul.Sandoz@Sun.Com
 */
public interface AsyncUniformInterface {

    /**
     * Invoke the HEAD method.
     * 
     * @return the HTTP response.
     */
    Future<ClientResponse> head();

    /**
     * Invoke the HEAD method.
     * 
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    Future<?> head(IAsyncListener<ClientResponse> l);
    
    
    /**
     * Invoke the OPTIONS method.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> options(Class<T> c);
    
    
    /**
     * Invoke the OPTIONS method.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> options(GenericType<T> gt);

    /**
     * Invoke the OPTIONS method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> options(IAsyncListener<T> l);

    /**
     * Invoke the GET method.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> get(Class<T> c) throws UniformInterfaceException;

    /**
     * Invoke the GET method.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> get(GenericType<T> gt) throws UniformInterfaceException;

    /**
     * Invoke the GET method.
     * 
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> get(IAsyncListener<T> l);
    
    /**
     * Invoke the PUT method with no request entity or response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     */
    Future<?> put();
    
    /**
     * Invoke the PUT method with a request entity but no response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @param requestEntity the request entity.
     * @return a void future.
     */
    Future<?> put(Object requestEntity);

    /**
     * Invoke the PUT method with no request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> put(Class<T> c);

    /**
     * Invoke the PUT method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> put(GenericType<T> gt);

    /**
     * Invoke the PUT method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> put(IAsyncListener<T> l);

    /**
     * Invoke the PUT method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> put(Class<T> c, Object requestEntity);
    
    /**
     * Invoke the PUT method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> put(GenericType<T> gt, Object requestEntity);

    /**
     * Invoke the PUT method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @param requestEntity the request entity.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> put(IAsyncListener<T> l, Object requestEntity);

    /**
     * Invoke the POST method with no request entity or response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     */
    Future<?> post();
    
    /**
     * Invoke the POST method with a request entity but no response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     * @param requestEntity the request entity.
     */
    Future<?> post(Object requestEntity);
    
    /**
     * Invoke the POST method with no request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> post(Class<T> c);

    /**
     * Invoke the POST method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> post(GenericType<T> gt);

    /**
     * Invoke the POST method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> post(IAsyncListener<T> l);

    /**
     * Invoke the POST method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> post(Class<T> c, Object requestEntity);

    /**
     * Invoke the POST method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> post(GenericType<T> gt, Object requestEntity);
    
    /**
     * Invoke the POST method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @param requestEntity the request entity.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> post(IAsyncListener<T> l, Object requestEntity);
            
    
    /**
     * Invoke the DELETE method with no request entity or response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     */
    Future<?> delete();
    
    /**
     * Invoke the DELETE method with a request entity but no response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     * @param requestEntity the request entity.
     */
    Future<?> delete(Object requestEntity);
    
    /**
     * Invoke the DELETE method with no request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> delete(Class<T> c);

    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> delete(GenericType<T> gt);

    /**
     * Invoke the DELETE method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> delete(IAsyncListener<T> l);

    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> delete(Class<T> c, Object requestEntity);

    /**
     * Invoke the DELETE method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> delete(GenericType<T> gt, Object requestEntity);
    
    /**
     * Invoke the DELETE method.
     *
     * @param <T> the type of the response.
     * @param l the listener to receive asynchronous callbacks.
     * @param requestEntity the request entity.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> delete(IAsyncListener<T> l, Object requestEntity);

    
    /**
     * Invoke a HTTP method with no request entity or response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     * @param method the HTTP method.
     */
    Future<?> method(String method);
    
    /**
     * Invoke a HTTP method with a request entity but no response.
     * <p>
     * If the status of the HTTP response is less than 300 and a representation
     * is present then that representation is ignored.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300.
     * 
     * @return a void future.
     * @param method the HTTP method.
     * @param requestEntity the request entity.
     */
    Future<?> method(String method, Object requestEntity);
    
    /**
     * Invoke a HTTP method with no request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> method(String method, Class<T> c);

    /**
     * Invoke a HTTP method with no request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param gt the generic type of the returned response.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> method(String method, GenericType<T> gt);

    /**
     * Invoke a HTTP method with no request entity that returns a response.
     *
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param l the listener to receive asynchronous callbacks.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> method(String method, IAsyncListener<T> l);

    /**
     * Invoke a HTTP method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>c</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param c the type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type <code>c</code>.
     */
    <T> Future<T> method(String method, Class<T> c, Object requestEntity);
    
    /**
     * Invoke a HTTP method with a request entity that returns a response.
     * <p>
     * The {@link Future#get} method will throw a UniformInterfaceException
     * if the status of the HTTP response is greater than or equal to 300 and
     * <code>gt</code> is not the type {@link ClientResponse}.
     * 
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param gt the generic type of the returned response.
     * @param requestEntity the request entity.
     * @return an instance of type represented by the generic type.
     */
    <T> Future<T> method(String method, GenericType<T> gt, Object requestEntity);

    /**
     * Invoke a HTTP method with a request entity that returns a response.
     *
     * @param <T> the type of the response.
     * @param method the HTTP method.
     * @param l the listener to receive asynchronous callbacks.
     * @param requestEntity the request entity.
     * @return A Future object that may be used to check the status of the
     *         request invocation. This object must not be used to try to
     *         obtain the results of the request, a null value will be returned
     *         from the call to {@link Future#get() }.
     */
    <T> Future<?> method(String method, IAsyncListener<T> l, Object requestEntity);
}