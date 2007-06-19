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

package com.sun.ws.rest.api.core;

/**
 * Low level interface for implementing web resource classes. Implmenting
 * classes must be annotated with <code>@UriTemplate</code> and may use
 * <code>@ConsumeMime</code> and <code>@ProduceMime</code> to filter the
 * requests they will receive.
 *
 * The container must honour annotations from the javax.annotation package. In
 * particular, resource class instance lifecycle can be managed using the
 * javax.annotation.PostConstruct and java.annotation.PreDestroy annotations
 * and a class can obtain access to container context information using 
 * javax.annotation.Resource as specified in JSR 250.
 *
 * @see javax.ws.rs.UriTemplate
 * @see javax.ws.rs.ConsumeMime
 * @see javax.ws.rs.ProduceMime
 */
public interface WebResource {
    
  /**
   * Called for each HTTP request
   * @param request the HTTP request information
   * @param response the HTTP response information
   */
  void handleRequest(HttpRequestContext request, HttpResponseContext response);
  
}
