/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.container.grizzly2;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.util.Charsets;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.server.impl.ThreadLocalInvoker;

import com.sun.jersey.spi.container.*;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

/**
 * Grizzly 2.0 Jersey container.
 *
 * @author Matt Swift
 * @author Jakub Podlesak
 */
public final class GrizzlyContainer extends HttpHandler implements
        ContainerListener {

    private static class ContextInjectableProvider<T> extends SingletonTypeInjectableProvider<Context, T> {

        protected ContextInjectableProvider(final Type type, final T instance) {
            super(type, instance);
        }
    }

    private final static class Writer implements ContainerResponseWriter {

        final Response response;

        Writer(final Response response) {
            this.response = response;
        }

        @Override
        public void finish() throws IOException {
        }

        @Override
        public OutputStream writeStatusAndHeaders(final long contentLength,
                final ContainerResponse cResponse) throws IOException {
            response.setStatus(cResponse.getStatus());

            if (contentLength != -1 && contentLength < Integer.MAX_VALUE) {
                response.setContentLength((int) contentLength);
            }

            for (final Map.Entry<String, List<Object>> e : cResponse.getHttpHeaders().entrySet()) {
                for (final Object value : e.getValue()) {
                    response.addHeader(e.getKey(),
                            ContainerResponse.getHeaderValue(value));
                }
            }

            final String contentType = response.getHeader("Content-Type");
            if (contentType != null) {
                response.setContentType(contentType);
            }

            return response.getOutputStream();
        }
    }

    private volatile WebApplication application;

    private final ThreadLocalInvoker<Request> requestInvoker =
            new ThreadLocalInvoker<Request>();

    private final ThreadLocalInvoker<Response> responseInvoker =
            new ThreadLocalInvoker<Response>();

    /**
     * Creates a new Grizzly container.
     *
     * @param resourceConfig
     *          The resource configuration.
     * @param application
     *          The Web application the container delegates to for the handling of
     *          HTTP requests.
     */
    GrizzlyContainer(final ResourceConfig resourceConfig,
            final WebApplication application) {
        this.application = application;

        setAllowEncodedSlash(resourceConfig.getFeature(GrizzlyServerFactory.AllowEncodedSlashFEATURE));

        final GenericEntity<ThreadLocal<Request>> requestThreadLocal =
                new GenericEntity<ThreadLocal<Request>>(
                requestInvoker.getImmutableThreadLocal()) {
                };

        resourceConfig.getSingletons().add(
                new ContextInjectableProvider<ThreadLocal<Request>>(
                requestThreadLocal.getType(), requestThreadLocal.getEntity()));

        final GenericEntity<ThreadLocal<Response>> responseThreadLocal =
                new GenericEntity<ThreadLocal<Response>>(
                responseInvoker.getImmutableThreadLocal()) {
                };

        resourceConfig.getSingletons().add(
                new ContextInjectableProvider<ThreadLocal<Response>>(
                responseThreadLocal.getType(), responseThreadLocal.getEntity()));
    }

    // ContainerListener

    @Override
    public void onReload() {
        final WebApplication oldApplication = application;
        application = application.clone();

        if (application.getFeaturesAndProperties() instanceof ReloadListener) {
            ((ReloadListener) application.getFeaturesAndProperties()).onReload();
        }

        oldApplication.destroy();
    }

    // HttpRequestProcessor

    @Override
    public void service(final Request request, final Response response) {
        try {
            requestInvoker.set(request);
            responseInvoker.set(response);

            _service(request, response);
        } finally {
            requestInvoker.set(null);
            responseInvoker.set(null);
        }
    }

    private void _service(final Request request, final Response response) {
        final WebApplication _application = application;

        final URI baseUri = getBaseUri(request);

        // TODO: this is terrible, there must be a way to obtain the original request URI!
        String originalURI = UriBuilder.fromPath(
                request.getRequest().getRequestURIRef()
                .getOriginalRequestURIBC().toString(Charsets.DEFAULT_CHARSET))
                .build().toString();

        String queryString = request.getQueryString();
        if (queryString != null) {
            originalURI = originalURI + "?" + queryString;
        }

        final URI requestUri = baseUri.resolve(originalURI);

        try {
            final ContainerRequest cRequest = new ContainerRequest(_application,
                    request.getMethod().getMethodString(), baseUri, requestUri,
                    getHeaders(request), request.getInputStream(true));

            _application.handleRequest(cRequest, new Writer(response));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private URI getBaseUri(final Request request) {
        try {
            return new URI(request.getScheme(), null, request.getServerName(),
                    request.getServerPort(), getBasePath(request), null, null);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private String getBasePath(final Request request) {
        final String contextPath = request.getContextPath();

        if (contextPath == null || contextPath.length() == 0) {
            return "/";
        } else if (contextPath.charAt(contextPath.length() - 1) != '/') {
            return contextPath + "/";
        } else {
            return contextPath;
        }
    }

    private InBoundHeaders getHeaders(final Request request) {
        final InBoundHeaders rh = new InBoundHeaders();
        for (final String name : request.getHeaderNames()) {
            rh.add(name, request.getHeader(name));
        }

        return rh;
    }
}
