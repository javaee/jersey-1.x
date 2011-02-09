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

package com.sun.jersey.test.framework.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.client.ClientFactory;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertTrue;

/**
 * @author pavel.bucek@oracle.com
 */
public class ClientFactoryTest extends JerseyTest {

    public class TestClientFactory implements ClientFactory {
        public boolean clientCreated = false;

        @Override
        public Client create(ClientConfig clientConfig) {
            clientCreated = true;

            return Client.create(clientConfig);
        }
    }

    private TestClientFactory tcc;

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new TestContainerFactory() {
            @Override
            public Class<WebAppDescriptor> supports() {
                return WebAppDescriptor.class;
            }

            @Override
            public TestContainer create(URI baseUri, AppDescriptor ad) throws IllegalArgumentException {
                return new TestContainer() {
                    @Override
                    public Client getClient() {
                        return null;
                    }

                    @Override
                    public URI getBaseUri() {
                        return null;
                    }

                    @Override
                    public void start() {
                    }

                    @Override
                    public void stop() {
                    }
                };
            }
        };
    }

    @Override
    protected ClientFactory getClientFactory() {
        tcc = new TestClientFactory();
        return tcc;
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder(getClass().getPackage().getName()).build();
    }

    @Test
    public void testClientFactory() {
        assertTrue(tcc.clientCreated);
    }
}
