/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.provider.CompletableReader;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.MessageBodyWorkers;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * A client (in-bound) HTTP response.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClientResponse {
    private static final Logger LOGGER = Logger.getLogger(ClientResponse.class.getName());

    /**
     * Status codes defined by HTTP, see
     * {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10">HTTP/1.1 documentation</a>}.
     * Additional status codes can be added by applications by creating an implementation of {@link StatusType}.
     */
    public enum Status implements StatusType {
        /**
         * 200 OK, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1">HTTP/1.1 documentation</a>}.
         */
        OK(200, "OK"),
        /**
         * 201 Created, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2">HTTP/1.1 documentation</a>}.
         */
        CREATED(201, "Created"),
        /**
         * 202 Accepted, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.3">HTTP/1.1 documentation</a>}.
         */
        ACCEPTED(202, "Accepted"),
        /**
         * 202 Accepted, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.4">HTTP/1.1 documentation</a>}.
         */
        NON_AUTHORITIVE_INFORMATION(203, "Non-Authoritative Information"),
        /**
         * 204 No Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5">HTTP/1.1 documentation</a>}.
         */
        NO_CONTENT(204, "No Content"),
        /**
         * 205 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.6">HTTP/1.1 documentation</a>}.
         */
        RESET_CONTENT(205, "Reset Content"),
        /**
         * 206 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.7">HTTP/1.1 documentation</a>}.
         */
        PARTIAL_CONTENT(206, "Partial Content"),


        /**
         * 301 Moved Permantely, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.2">HTTP/1.1 documentation</a>}.
         */
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        /**
         * 302 Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">HTTP/1.1 documentation</a>}.
         */
        FOUND(302, "Found"),
        /**
         * 303 See Other, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.4">HTTP/1.1 documentation</a>}.
         */
        SEE_OTHER(303, "See Other"),
        /**
         * 304 Not Modified, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5">HTTP/1.1 documentation</a>}.
         */
        NOT_MODIFIED(304, "Not Modified"),
        /**
         * 305 Use Proxy, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.6">HTTP/1.1 documentation</a>}.
         */
        USE_PROXY(305, "Use Proxy"),
        /**
         * 307 Temporary Redirect, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.8">HTTP/1.1 documentation</a>}.
         */
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),


        /**
         * 400 Bad Request, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1">HTTP/1.1 documentation</a>}.
         */
        BAD_REQUEST(400, "Bad Request"),
        /**
         * 401 Unauthorized, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2">HTTP/1.1 documentation</a>}.
         */
        UNAUTHORIZED(401, "Unauthorized"),
        /**
         * 402 Payment Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.3">HTTP/1.1 documentation</a>}.
         */
        PAYMENT_REQUIRED(402, "Payment Required"),
        /**
         * 403 Forbidden, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.4">HTTP/1.1 documentation</a>}.
         */
        FORBIDDEN(403, "Forbidden"),
        /**
         * 404 Not Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5">HTTP/1.1 documentation</a>}.
         */
        NOT_FOUND(404, "Not Found"),
        /**
         * 405 Method Not Allowed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">HTTP/1.1 documentation</a>}.
         */
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        /**
         * 406 Not Acceptable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7">HTTP/1.1 documentation</a>}.
         */
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        /**
         * 407 Proxy Authentication Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.8">HTTP/1.1 documentation</a>}.
         */
        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
        /**
         * 408 Request Timeout, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.9">HTTP/1.1 documentation</a>}.
         */
        REQUEST_TIMEOUT(408, "Request Timeout"),
        /**
         * 409 Conflict, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10">HTTP/1.1 documentation</a>}.
         */
        CONFLICT(409, "Conflict"),
        /**
         * 410 Gone, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11">HTTP/1.1 documentation</a>}.
         */
        GONE(410, "Gone"),
        /**
         * 411 Length Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.12">HTTP/1.1 documentation</a>}.
         */
        LENGTH_REQUIRED(411, "Length Required"),
        /**
         * 412 Precondition Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.13">HTTP/1.1 documentation</a>}.
         */
        PRECONDITION_FAILED(412, "Precondition Failed"),
        /**
         * 413 Request Entity Too Large, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.14">HTTP/1.1 documentation</a>}.
         */
        REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
        /**
         * 414 Request-URI Too Long, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.15">HTTP/1.1 documentation</a>}.
         */
        REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
        /**
         * 415 Unsupported Media Type, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.16">HTTP/1.1 documentation</a>}.
         */
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
        /**
         * 416 Requested Range Not Satisfiable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.17">HTTP/1.1 documentation</a>}.
         */
        REQUESTED_RANGE_NOT_SATIFIABLE(416, "Requested Range Not Satisfiable"),
        /**
         * 417 Expectation Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.18">HTTP/1.1 documentation</a>}.
         */
        EXPECTATION_FAILED(417, "Expectation Failed"),


        /**
         * 500 Internal Server Error, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1">HTTP/1.1 documentation</a>}.
         */
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        /**
         * 501 Not Implemented, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2">HTTP/1.1 documentation</a>}.
         */
        NOT_IMPLEMENTED(501, "Not Implemented"),
        /**
         * 502 Bad Gateway, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.3">HTTP/1.1 documentation</a>}.
         */
        BAD_GATEWAY(502, "Bad Gateway"),
        /**
         * 503 Service Unavailable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.4">HTTP/1.1 documentation</a>}.
         */
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        /**
         * 504 Gateway Timeout, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.5">HTTP/1.1 documentation</a>}.
         */
        GATEWAY_TIMEOUT(504, "Gateway Timeout"),
        /**
         * 505 HTTP Version Not Supported, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.6">HTTP/1.1 documentation</a>}.
         */
        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");


        private final int code;
        private final String reason;
        private Family family;

        Status(final int statusCode, final String reasonPhrase) {
            this.code = statusCode;
            this.reason = reasonPhrase;
            switch(code/100) {
                case 1: this.family = Family.INFORMATIONAL; break;
                case 2: this.family = Family.SUCCESSFUL; break;
                case 3: this.family = Family.REDIRECTION; break;
                case 4: this.family = Family.CLIENT_ERROR; break;
                case 5: this.family = Family.SERVER_ERROR; break;
                default: this.family = Family.OTHER; break;
            }
        }

        /**
         * Get the class of status code.
         *
         * @return the class of status code.
         */
        public Family getFamily() {
            return family;
        }

        /**
         * Get the associated status code.
         *
         * @return the status code
         */
        public int getStatusCode() {
            return code;
        }

        /**
         * Get the reason phrase.
         *
         * @return the reason phrase.
         */
        public String getReasonPhrase() {
            return toString();
        }

        /**
         * Get the reason phrase.
         *
         * @return the reason phrase.
         */
        @Override
        public String toString() {
            return reason;
        }

        /**
         * Convert a numerical status code into the corresponding Status.
         *
         * @param statusCode the numerical status code.
         * @return the matching Status or null is no matching Status is defined.
         */
        public static Status fromStatusCode(final int statusCode) {
            for (Status s : Status.values()) {
                if (s.code == statusCode) {
                    return s;
                }
            }
            return null;
        }
    }

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    protected static final HeaderDelegate<EntityTag> entityTagDelegate =
            RuntimeDelegate.getInstance().createHeaderDelegate(EntityTag.class);

    protected static final HeaderDelegate<Date> dateDelegate =
            RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);

    private Map<String, Object> properties;

    private int status;

    private InBoundHeaders headers;

    private boolean isEntityBuffered;

    private InputStream entity;

    private MessageBodyWorkers workers;

    public ClientResponse(int status, InBoundHeaders headers, InputStream entity, MessageBodyWorkers workers) {
        this.status = status;
        this.headers = headers;
        this.entity = entity;
        this.workers = workers;
    }

    /**
     * Get the client.
     *
     * @return the client.
     */
    public Client getClient() {
        return (Client)getProperties().get(Client.class.getName());
    }

    /**
     * Get the map of response properties.
     * <p>
     * A response property is an application-defined property that may be
     * added by the user, a filter, or the handler that is managing the
     * connection.
     *
     * @return the map of response properties.
     */
    public Map<String, Object> getProperties() {
        if (properties != null) return properties;

        return properties = new HashMap<String, Object>();
    }

    /**
     * Get the status code.
     *
     * @return the status code.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the status code.
     *
     * @param status the status code.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Set the status code.
     *
     * @param status the status code.
     */
    public void setStatus(Response.StatusType status) {
        setStatus(status.getStatusCode());
    }

    /**
     * Get the status code.
     *
     * @return the status code, or null if the underlying status code was set
     *         using the method {@link #setStatus(int)} and there is no
     *         mapping between the integer value and the Response.Status
     *         enumeration value.
     */
    public Status getClientResponseStatus() {
        return Status.fromStatusCode(status);
    }

    /**
     * Get the status code.
     *
     * @return the status code, or null if the underlying status code was set
     *         using the method {@link #setStatus(int)} and there is no
     *         mapping between the integer value and the Response.Status
     *         enumeration value.
     * @deprecated use {@link #getClientResponseStatus() }
     */
    @Deprecated
    public Response.Status getResponseStatus() {
        return Response.Status.fromStatusCode(status);
    }

    /**
     * Set the status code.
     *
     * @param status the status code.
     * @deprecated see {@link #setStatus(javax.ws.rs.core.Response.StatusType) }
     */
    @Deprecated
    public void setResponseStatus(Response.StatusType status) {
        setStatus(status);
    }

    /**
     * Get the HTTP headers of the response.
     *
     * @return the HTTP headers of the response.
     * @deprecated
     */
    @Deprecated
    public MultivaluedMap<String, String> getMetadata() {
        return getHeaders();
    }

    /**
     * Get the HTTP headers of the response.
     *
     * @return the HTTP headers of the response.
     */
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Checks if there is an entity available.
     *
     * @return true if there is an entity present in the response.
     */
    public boolean hasEntity() {
        try {
            if (entity.available() > 0) {
                return true;
            } else if (entity.markSupported()) {
                entity.mark(1);
                int i = entity.read();
                entity.reset();
                return i != -1;
            } else {
                int b = entity.read();
                if (b == -1) {
                    return false;
                }

                if (!(entity instanceof PushbackInputStream)) {
                    entity = new PushbackInputStream(entity, 1);
                }
                ((PushbackInputStream) entity).unread(b);

                return true;
            }
        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
    }

    /**
     * Get the input stream of the response.
     *
     * @return the input stream of the response.
     */
    public InputStream getEntityInputStream() {
        return entity;
    }

    /**
     * Set the input stream of the response.
     *
     * @param entity the input stream of the response.
     */
    public void setEntityInputStream(InputStream entity) {
        this.isEntityBuffered = false;
        this.entity = entity;
    }

    /**
     * Get the entity of the response.
     * <p>
     * If the entity is not an instance of Closeable then the entity
     * input stream is closed.
     *
     * @param <T> the type of the response.
     * @param c the type of the entity.
     * @return an instance of the type <code>c</code>.
     *
     * @throws ClientHandlerException if there is an error processing the response.
     * @throws UniformInterfaceException if the response status is 204 (No Content).
     */
    public <T> T getEntity(Class<T> c) throws ClientHandlerException, UniformInterfaceException {
        return getEntity(c, c);
    }

    /**
     * Get the entity of the response.
     * <p>
     * If the entity is not an instance of Closeable then this response
     * is closed (you cannot read it more than once, any subsequent
     * call will produce {@link ClientHandlerException}).
     *
     * @param <T> the type of the response.
     * @param gt the generic type of the entity.
     * @return an instance of the type represented by the generic type.
     *
     * @throws ClientHandlerException if there is an error processing the response.
     * @throws UniformInterfaceException if the response status is 204 (No Content).
     */
    public <T> T getEntity(GenericType<T> gt) throws ClientHandlerException, UniformInterfaceException {
        return getEntity(gt.getRawClass(), gt.getType());
    }

    private <T> T getEntity(Class<T> c, Type type) {
        if (getStatus() == 204) {
            throw new UniformInterfaceException(this);
        }

        MediaType mediaType = getType();
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }

        final MessageBodyReader<T> br = workers.getMessageBodyReader(
                c, type,
                EMPTY_ANNOTATIONS, mediaType);
        if (br == null) {
            close();
            String message = "A message body reader for Java class " + c.getName() +
                    ", and Java type " + type +
                    ", and MIME media type " + mediaType + " was not found";
            LOGGER.severe(message);
            Map<MediaType, List<MessageBodyReader>> m = workers.getReaders(mediaType);
            LOGGER.severe("The registered message body readers compatible with the MIME media type are:\n" +
                    workers.readersToString(m));

            throw new ClientHandlerException(message);
        }

        try {
            T t = br.readFrom(c, type, EMPTY_ANNOTATIONS, mediaType, headers, entity);
            if (br instanceof CompletableReader) {
                t = ((CompletableReader<T>)br).complete(t);
            }
            if (!(t instanceof Closeable)) {
                close();
            }
            return t;
        } catch (IOException ex) {
            close();
            throw new ClientHandlerException(ex);
        }
    }

    /**
     * Buffer the entity.
     * <p>
     * All the bytes of the original entity input stream will be read
     * and stored in memory. The original entity input stream will
     * then be closed.
     * @throws ClientHandlerException if there is an error processing the response.
     */
    public void bufferEntity() throws ClientHandlerException {
        if (isEntityBuffered)
            return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ReaderWriter.writeTo(entity, baos);
        } catch(IOException ex) {
            throw new ClientHandlerException(ex);
        } finally {
            close();
        }

        entity = new ByteArrayInputStream(baos.toByteArray());
        isEntityBuffered = true;
    }

    /**
     * Close the response.
     * <p>
     * The entity input stream is closed.
     *
     * @throws ClientHandlerException if there is an error closing the response.
     */
    public void close() throws ClientHandlerException {
        try {
            entity.close();
        } catch (IOException e) {
            throw new ClientHandlerException(e);
        }
    }

    /**
     * Get the media type of the response.
     *
     * @return the media type.
     */
    public MediaType getType() {
        String ct = getHeaders().getFirst("Content-Type");
        return (ct != null) ? MediaType.valueOf(ct) : null;
    }

    /**
     * Get the location.
     *
     * @return the location, otherwise <code>null</code> if not present.
     */
    public URI getLocation() {
        String l = getHeaders().getFirst("Location");
        return (l != null) ? URI.create(l) : null;
    }

    /**
     * Get the entity tag.
     *
     * @return the entity tag, otherwise <code>null</code> if not present.
     */
    public EntityTag getEntityTag() {
        String t = getHeaders().getFirst("ETag");

        return (t != null) ? entityTagDelegate.fromString(t) : null;
    }

    /**
     * Get the last modified date.
     *
     * @return the last modified date, otherwise <code>null</code> if not present.
     */
    public Date getLastModified() {
        String d = getHeaders().getFirst("Last-Modified");

        return (d != null) ? dateDelegate.fromString(d) : null;
    }

    /**
     * Get response date (server side).
     *
     * @return the server side response date, otherwise <code>null</code> if not present.
     */
    public Date getResponseDate() {
        String d = getHeaders().getFirst("Date");

        return (d != null) ? dateDelegate.fromString(d) : null;
    }

    /**
     * Get the language.
     *
     * @return the language, otherwise <code>null</code> if not present.
     */
    public String getLanguage() {
        return getHeaders().getFirst("Content-Language");
    }

    /**
     * Get Content-Length.
     *
     * @return Content-Length as integer if present and valid number. In other
     * cases returns -1.
     */
    public int getLength() {
        int size = -1;

        String sizeStr = getHeaders().getFirst("Content-Length");
        if (sizeStr == null)
            return -1;

        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException nfe) {
            // do nothing
        }

        return size;
    }

    /**
     * Get the list of cookies.
     *
     * @return the cookies.
     */
    public List<NewCookie> getCookies() {
        List<String> hs = getHeaders().get("Set-Cookie");
        if (hs == null) return Collections.emptyList();

        List<NewCookie> cs = new ArrayList<NewCookie>();
        for (String h : hs) {
            cs.add(NewCookie.valueOf(h));
        }
        return cs;
    }

    /**
     * Get the allowed HTTP methods from the Allow HTTP header.
     * <p>
     * Note that the Allow HTTP header will be returned from an OPTIONS
     * request.
     *
     * @return the allowed HTTP methods, all methods will returned as
     *         upper case strings.
     */
    public Set<String> getAllow() {
        String allow = headers.getFirst("Allow");
        if (allow == null)
            return Collections.emptySet();

        Set<String> allowedMethods = new HashSet<String>();
        StringTokenizer tokenizer = new StringTokenizer(allow, ",");
        while (tokenizer.hasMoreTokens()) {
            String m = tokenizer.nextToken().trim();
            if (m.length() > 0)
                allowedMethods.add(m.toUpperCase());
        }
        return allowedMethods;
    }

    public WebResourceLinkHeaders getLinks() {
        return new WebResourceLinkHeaders(getClient(), getHeaders());
    }

    @Override
    public String toString() {
        return "Client response status: " + status;
    }
}
