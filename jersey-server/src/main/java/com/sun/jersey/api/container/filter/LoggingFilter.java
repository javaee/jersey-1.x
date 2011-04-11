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
package com.sun.jersey.api.container.filter;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A logging filter.
 * <p>
 * The request headers, request entity, response headers and response entity
 * will be logged. By default logging will be output to System.out.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey filter can be
 * registered using the following initialization parameters:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.LoggingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 *     &lt;init-param&gt
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerResponseFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.LoggingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * </pre></blockquote>
 * <p>
 * The logging of entities may be disabled by setting the feature
 * {@link #FEATURE_LOGGING_DISABLE_ENTITY} to true. When an application is
 * deployed as a Servlet or Filter this feature can be
 * registered using the following initialization parameter:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.config.feature.logging.DisableEntitylogging&lt;/param-name&gt;
 *         &lt;param-value&gt;true&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 *
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.container.filter
 */
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    /**
     * If true the request and response entities (if present) will not be logged.
     * If false the request and response entities will be logged.
     * <p>
     * The default value is false.
     */
    public static final String FEATURE_LOGGING_DISABLE_ENTITY
            = "com.sun.jersey.config.feature.logging.DisableEntitylogging";

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    private static final String NOTIFICATION_PREFIX = "* ";
    
    private static final String REQUEST_PREFIX = "> ";
    
    private static final String RESPONSE_PREFIX = "< ";

    private final Logger logger;

    private @Context HttpContext hc;
    
    private @Context ResourceConfig rc;

    private long id = 0;

    /**
     * Create a logging filter logging the request and response to
     * a default JDK logger, named as the fully qualified class name of this
     * class.
     */
    public LoggingFilter() {
        this(LOGGER);
    }

    /**
     * Create a logging filter logging the request and response to
     * a JDK logger.
     *
     * @param logger the logger to log requests and responses.
     */
    public LoggingFilter(Logger logger) {
        this.logger = logger;
    }

    private synchronized void setId() {
        if ( hc.getProperties().get("request-id") == null) {
            hc.getProperties().put("request-id", Long.toString(++id));
        }
    }

    private StringBuilder prefixId(StringBuilder b) {
        b.append(hc.getProperties().get("request-id").toString()).
                append(" ");
        return b;
    }
    
    public ContainerRequest filter(ContainerRequest request) {
        setId();

        final StringBuilder b = new StringBuilder();
        printRequestLine(b, request);
        printRequestHeaders(b, request.getRequestHeaders());

        if (rc.getFeature(FEATURE_LOGGING_DISABLE_ENTITY)) {
            logger.info(b.toString());
            return request;
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = request.getEntityInputStream();
            try {
                if(in.available() > 0) {
                    ReaderWriter.writeTo(in, out);

                    byte[] requestEntity = out.toByteArray();
                    printEntity(b, requestEntity);

                    request.setEntityInputStream(new ByteArrayInputStream(requestEntity));
                }
                return request;
            } catch (IOException ex) {
                throw new ContainerException(ex);
            } finally {
                logger.info(b.toString());
            }
        }
    }
    
    private void printRequestLine(StringBuilder b, ContainerRequest request) {
        prefixId(b).append(NOTIFICATION_PREFIX).append("Server in-bound request").append('\n');
        prefixId(b).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").
                append(request.getRequestUri().toASCIIString()).append('\n');
    }
    
    private void printRequestHeaders(StringBuilder b, MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId(b).append(REQUEST_PREFIX).append(header).append(": ").
                        append(value).append('\n');
            }
        }
        prefixId(b).append(REQUEST_PREFIX).append('\n');
    }

    private void printEntity(StringBuilder b, byte[] entity) throws IOException {
        if (entity.length == 0)
            return;
        b.append(new String(entity)).append("\n");
    }

    private final class Adapter implements ContainerResponseWriter {
        private final ContainerResponseWriter crw;

        private final boolean disableEntity;

        private long contentLength;

        private ContainerResponse response;

        private ByteArrayOutputStream baos;

        private StringBuilder b = new StringBuilder();

        Adapter(ContainerResponseWriter crw) {
            this.crw = crw;
            this.disableEntity = rc.getFeature(FEATURE_LOGGING_DISABLE_ENTITY);
        }
        
        public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
            printResponseLine(b, response);
            printResponseHeaders(b, response.getHttpHeaders());

            if (disableEntity) {
                logger.info(b.toString());
                return crw.writeStatusAndHeaders(contentLength, response);
            } else {
                this.contentLength = contentLength;
                this.response = response;
                return this.baos = new ByteArrayOutputStream();
            }
        }

        public void finish() throws IOException {
            if (!disableEntity) {
                byte[] entity = baos.toByteArray();
                printEntity(b, entity);

                // Output to log
                logger.info(b.toString());

                // Write out the headers and buffered entity
                OutputStream out = crw.writeStatusAndHeaders(contentLength, response);
                out.write(entity);
            }
            crw.finish();
        }
    }

    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        setId();
        response.setContainerResponseWriter(
                new Adapter(response.getContainerResponseWriter()));
        return response;
    }
    
    private void printResponseLine(StringBuilder b, ContainerResponse response) {
        prefixId(b).append(NOTIFICATION_PREFIX).
            append("Server out-bound response").append('\n');
        prefixId(b).append(RESPONSE_PREFIX).append(Integer.toString(response.getStatus())).append('\n');
    }
    
    private void printResponseHeaders(StringBuilder b, MultivaluedMap<String, Object> headers) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            String header = e.getKey();
            for (Object value : e.getValue()) {
                prefixId(b).append(RESPONSE_PREFIX).append(header).append(": ").
                        append(ContainerResponse.getHeaderValue(value)).append('\n');
            }
        }
        prefixId(b).append(RESPONSE_PREFIX).append('\n');
    } 
}