/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.api;

import java.net.URI;
import javax.ws.rs.WebApplicationException;

/**
 * A HTTP 404 (Not Found) exception.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class NotFoundException extends WebApplicationException {

    private final URI notFoundUri;

    /**
     * Create a HTTP 404 (Not Found) exception.
     */
    public NotFoundException() {
        this((URI)null);
    }
    
    /**
     * Create a HTTP 404 (Not Found) exception.
     * 
     * @param notFoundUri the URI that cannot be found.
     */
    public NotFoundException(URI notFoundUri) {
        super(Responses.notFound().build());
        this.notFoundUri = notFoundUri;
    }

    /**
     * Create a HTTP 404 (Not Found) exception.
     *
     * @param message the String that is the entity of the 404 response.
     */
    public NotFoundException(String message) {
        this(message, null);
    }
    
    /**
     * Create a HTTP 404 (Not Found) exception.
     *
     * @param message the String that is the entity of the 404 response.
     * @param notFoundUri the URI that cannot be found.
     */
    public NotFoundException(String message, URI notFoundUri) {
        super(Responses.notFound().
                entity(message).type("text/plain").build());
        this.notFoundUri = notFoundUri;
    }

    /**
     * Get the URI that is not found.
     *
     * @return the URI that is not found.
     */
    public URI getNotFoundUri() {
        return notFoundUri;
    }

    @Override    
    public String getMessage() {
        return super.getMessage() + " for uri: " + notFoundUri;
    }
}