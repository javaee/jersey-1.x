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

package com.sun.ws.rest.impl.model.method.dispatch;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.impl.model.MediaTypeList;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceJavaMethodDispatcher implements RequestDispatcher {

    protected final Method method;
    
    final private MediaTypeList produceMime;
    
    final private MediaType mediaType;

    public ResourceJavaMethodDispatcher(AbstractResourceMethod abstractResourceMethod) {
        this.method = abstractResourceMethod.getMethod();
        this.produceMime = new MediaTypeList();
        this.produceMime.addAll(abstractResourceMethod.getSupportedOutputTypes());
        
        if (this.produceMime.size() == 1) {
            MediaType c = this.produceMime.get(0);
            if (c.getType().equals("*") || c.getSubtype().equals("*")) 
                mediaType = null;
            else
                mediaType = this.produceMime.get(0);
        } else {
            mediaType = null;
        }        
    }    
    
    public final void dispatch(Object resource, 
            HttpRequestContext request, HttpResponseContext response) {
        // Invoke the method on the resource
        try {
            _dispatch(resource, request, response);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                // Rethrow the runtime exception
                throw (RuntimeException)t;
            } else {
                // TODO should a checked exception be wrapped in 
                // WebApplicationException ?
                throw new ContainerException(t);
            }
        } catch (IllegalAccessException e) {
            throw new ContainerException(e);
        }
    }
    
    protected final MediaType getAcceptableMediaType(HttpRequestContext requestContext) {
        if (produceMime.size() == 1) {
            return mediaType;
        } else {
            MediaType m = requestContext.getAcceptableMediaType(produceMime);
            if (m != null) {
                if (m.getType().equals(MediaType.MEDIA_TYPE_WILDCARD) ||
                        m.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD))
                    return null;
            }

            return m;
        }
    }
    
    protected abstract void _dispatch(Object resource, 
            HttpRequestContext request, HttpResponseContext response) 
            throws IllegalAccessException, InvocationTargetException;
}
