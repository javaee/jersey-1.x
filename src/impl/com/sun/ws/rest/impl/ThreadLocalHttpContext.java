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

package com.sun.ws.rest.impl;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.core.Response;

/**
 * Implementation of {@link HttpContextAccess} using {@link ThreadLocal}
 * to store {@link HttpRequestContext} and {@link HttpResponseContext} instances
 * associated with threads.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ThreadLocalHttpContext implements HttpContextAccess {
    private ThreadLocal<HttpContextAccess> context = new ThreadLocal<HttpContextAccess>();

    /**
     * Set the {@link HttpRequestContext} and {@link HttpResponseContext} instances
     * for the current thread.
     */
    public void set(HttpContextAccess context) {
        this.context.set(context);
    }

    public HttpRequestContext getHttpRequestContext() {
        return context.get().getHttpRequestContext();
    }

    public HttpResponseContext getHttpResponseContext() {
        return context.get().getHttpResponseContext();
    }

    public Response createLocalForward(String path) throws ContainerException {
        return context.get().createLocalForward(path);
    }
}
