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
/**
 * Provides support for client-side communication with HTTP-based
 * RESTful Web services.
 * <p>
 * The client API is high-level API that reuses many aspects of the JAX-RS
 * API. It is designed to be quick, easy to use and is especially useful
 * and productive when developing tests for Web services.
 * <p>
 * The client API can be used as follows to make simple GET and POST requests
 * to a Web resource:
 * <blockquote><pre>
 *     Client c = Client.create();
 *     WebResource r = c.resource("http://host/base");
 *     String s = r.get(String.class);
 *     s = r.post(String.class, s);
 * </pre></blockquote>
 * <p>
 * A key concept of REST is the uniform interface and this is encapsulated in
 * the {@link com.sun.jersey.api.client.WebResource} class, which implements 
 * {@link com.sun.jersey.api.client.UniformInterface}.
 * A request is built up and when the corresponding HTTP method on the
 * {@link com.sun.jersey.api.client.UniformInterface} is invoked the request
 * is sent to the Web resource and the returned response is processed. This enables
 * efficient production of requests as follows:
 * <blockquote><pre>
 *     WebResource r = ...
 *     String s = r.accept("application/xml").get(String.class);
 *     s = r.accept("application/xml").type("application/xml").post(String.class, s);
 * </pre></blockquote>
 * In the above example a GET request occurs stating that the
 * "application/xml" is acceptable. After that a POST request occurs stating
 * the same acceptable media type and that the content type of the request entity is
 * "application/xml".
 * <p>
 * The Java types that may be used for request and response entities are not 
 * restricted to <code>String</code>. The client API reuses the same infrastrucure 
 * as JAX-RS server-side. Thus the same types that can be used on the server-side
 * can also be used on the client-side, such as JAXB-based types. Further more
 * the supported Java types can be extended by implementing 
 * {@link javax.ws.rs.ext.MessageBodyReader} and
 * {@link javax.ws.rs.ext.MessageBodyWriter}. For registration of such support
 * for new Java types see
 * {@link com.sun.jersey.api.client.config.ClientConfig}.
 * <p>
 * A type of {@link com.sun.jersey.api.client.ClientResponse} declared
 * for the response entity may be used to obtain the status, headers and
 * response entity.
 * <p>
 * If any type, other than {@link com.sun.jersey.api.client.ClientResponse},
 * is declared and the response status is greater than or equal to 300 then a
 * {@link com.sun.jersey.api.client.UniformInterfaceException} exception
 * will be thrown, from which the 
 * {@link com.sun.jersey.api.client.ClientResponse} instance can be
 * accessed.
 * <p>
 * In the following cases it is necessary to close the response, when response
 * processing has completed, to ensure that underlying resources are
 * correctly released.
 * <p>
 * If a response entity is declared of the type
 * {@link com.sun.jersey.api.client.ClientResponse}
 * or of a type that is assignable to {@link java.io.Closeable}
 * (such as {@link java.io.InputStream}) then the response must be either: 
 * 1) closed by invoking the method
 * {@link com.sun.jersey.api.client.ClientResponse#close() } or
 * {@link java.io.Closeable#close}; or 2) all bytes of response entity must be
 * read.
 * <p>
 * If a {@link com.sun.jersey.api.client.UniformInterfaceException} is
 * thrown then by default the response entity is automatically buffered and
 * the underlying resources are correctly released. See the following property
 * for more details:
 * {@link com.sun.jersey.api.client.config.ClientConfig#PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION}.
 *
 */
package com.sun.jersey.api.client;