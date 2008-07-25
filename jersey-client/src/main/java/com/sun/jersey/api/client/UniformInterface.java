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