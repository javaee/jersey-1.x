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

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A client (in-bound) HTTP response.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ClientResponse {
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
     * Get the status code.
     * 
     * @return the status code, or null if the underlying status code was set
     *         using the method {@link #setStatus(int)} and there is no
     *         mapping between the the integer value and the Response.Status
     *         enumeration value.
     */
    public Response.Status getResponseStatus() {
        return Response.Status.fromStatusCode(status);
    }
    
    /**
     * Set the status code.
     * 
     * @param status the status code.
     */
    public void setResponseStatus(Response.Status status) {
        setStatus(status.getStatusCode());
    }
    
    /**
     * Get the HTTP headers of the response.
     * 
     * @return the HTTP headers of the response.
     */
    public MultivaluedMap<String, String> getMetadata() {
        return headers;
    }

    /**
     * 
     * @return true if there is an entity present in the response.
     */
    public boolean hasEntity() {
        try {
            return entity.available() > 0;
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
     * @throws UniformInterfaceException if the response status is 204 (No Contnet).
     */
    public <T> T getEntity(Class<T> c) throws ClientHandlerException, UniformInterfaceException {
        return getEntity(c, c);
    }

    /**
     * Get the entity of the response.
     * <p>
     * If the entity is not an instance of Closeable then this response
     * is closed.
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

        try {
            MediaType mediaType = getType();
            if (mediaType == null) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }
            
            final MessageBodyReader<T> br = workers.getMessageBodyReader(
                    c, type,
                    EMPTY_ANNOTATIONS, mediaType);
            if (br == null) {
                throw new ClientHandlerException(
                        "A message body reader for Java type, " + c +
                        ", and MIME media type, " + mediaType + ", was not found");
            }
            T t = br.readFrom(c, type, EMPTY_ANNOTATIONS, mediaType, headers, entity);
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
        byte[] b = new byte[4096];
        int l;
        try {
            while ((l = entity.read(b)) != -1) {
                baos.write(b, 0, l);
            }
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
        String ct = getMetadata().getFirst("Content-Type");
        return (ct != null) ? MediaType.valueOf(ct) : null;
    }
    
    /**
     * Get the location.
     * 
     * @return the location.
     */
    public URI getLocation() {
        String l = getMetadata().getFirst("Location");        
        return (l != null) ? URI.create(l) : null;
    }
    
    /**
     * Get the entity tag.
     * 
     * @return the entity tag.
     */
    public EntityTag getEntityTag() {
        String t = getMetadata().getFirst("ETag");
        
        return (t != null) ? entityTagDelegate.fromString(t) : null;
    }

    /**
     * Get the last modified date.
     * 
     * @return the last modified date.
     */
    public Date getLastModified() {
        String d = getMetadata().getFirst("Last-Modified");
        
        return (d != null) ? dateDelegate.fromString(d) : null;
    }

    /**
     * Get response date (server side).
     *
     * @return the server side response date.
     */
    public Date getResponseDate() {
        String d = getMetadata().getFirst("Date");

        return (d != null) ? dateDelegate.fromString(d) : null;
    }
    
    /**
     * Get the language.
     * 
     * @return the language.
     */
    public String getLanguage() {
        return getMetadata().getFirst("Content-Language");
    }

    /**
     * Get Content-Length.
     *
     * @return Content-Length as integer if present and valid number. In other
     * cases returns -1.
     */
    public int getLength() {
        int size = -1;

        String sizeStr = getMetadata().getFirst("Content-Length");

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
        List<String> hs = getMetadata().get("Set-Cookie");
        if (hs == null) return Collections.emptyList();
        
        List<NewCookie> cs = new ArrayList<NewCookie>();
        for (String h : hs) {
            cs.add(NewCookie.valueOf(h));
        }
        return cs;
    }

    @Override
    public String toString() {
        return "Client response status: " + status;
    }
}