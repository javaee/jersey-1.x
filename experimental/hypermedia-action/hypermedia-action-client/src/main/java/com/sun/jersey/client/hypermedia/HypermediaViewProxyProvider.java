/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.client.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.impl.ClientRequestImpl;
import com.sun.jersey.client.proxy.ViewProxy;
import com.sun.jersey.client.proxy.ViewProxyProvider;
import com.sun.jersey.core.hypermedia.HypermediaController;
import java.lang.reflect.Proxy;

/**
 * Hypermedia View Proxy Provider.
 * 
 * @author Santiago.PericasGeertsen@sun.com
 * @author Paul.Sandoz@sun.com
 */
public class HypermediaViewProxyProvider implements ViewProxyProvider {

    public <T> ViewProxy<T> proxy(final Client client, final Class<T> ctlr) {
        if (!ctlr.isAnnotationPresent(HypermediaController.class))
            return null;

        return new ViewProxy<T>() {

            public T view(Class<T> type, ClientRequest request, ClientHandler handler) {
                HypermediaController ctrl = ctlr.getAnnotation(HypermediaController.class);
                Class<?> c = ctrl.model();

                ClientRequest ro = new ClientRequestImpl(request.getURI(), request.getMethod());
                ClientResponse r = handler.handle(ro);
                Object instance;

                if (c == ClientResponse.class) {
                    instance = c.cast(r);
                } else if (r.getStatus() < 300) {
                    instance = r.getEntity(c);
                } else {
                    throw new UniformInterfaceException(r,
                        ro.getPropertyAsFeature(
                            ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
                }

                return (T) Proxy.newProxyInstance(ctlr.getClassLoader(),
                        new Class[] { ctlr },
                        new ControllerInvocationHandler(client, instance, r, ctlr));
            }

            public T view(T v, ClientRequest request, ClientHandler handler) {
                throw new UnsupportedOperationException();
            }

            public T view(Class<T> type, ClientResponse response) {
                throw new UnsupportedOperationException();
            }

            public T view(T v, ClientResponse cr) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
