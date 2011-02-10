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
package com.sun.jersey.client.urlconnection;

import com.sun.jersey.api.client.Client;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A factory for {@link HttpURLConnection} instances.
 * <p>
 * A factory may be used to create a {@link HttpURLConnection} and configure
 * it in a custom manner that is not possible using the Client API.
 * <p>
 * A factory instance may be registered with the constructor
 * {@link URLConnectionClientHandler#URLConnectionClientHandler(com.sun.jersey.client.urlconnection.HttpURLConnectionFactory)}.
 * Then the {@link URLConnectionClientHandler} instance may be registered with a {@link Client}
 * using the constructor {@link Client#Client(com.sun.jersey.api.client.ClientHandler) }.
 *
 * @author pavel.bucek@sun.com
 */
public interface HttpURLConnectionFactory {

    /**
     * Get a {@link HttpURLConnection} for a given URL.
     * <p>
     * This method is re-entrant so implementation should not return the same
     * {@link HttpURLConnection} instance for multiple and concurrent requests.
     * 
     * @param url the URL.
     * @return the {@link HttpURLConnection}.
     * @throws java.io.IOException
     */
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException;
}