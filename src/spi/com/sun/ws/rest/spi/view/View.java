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

package com.sun.ws.rest.spi.view;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.spi.container.ContainerResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A view (connetected to a resource) that is capable of producing a 
 * representation.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface View {

    /**
     * Process a view writing the contents of processing to the {@link OutputStream}
     * obtained from {@link ContainerResponse#getOutputStream()}
     *
     * @param it the object to be passed to the view.
     * @param request the HTTP request.
     * @param response the HTTP response.
     */
    void process(Object it, 
            HttpRequestContext request, HttpResponseContext response) throws IOException, ContainerException;
}