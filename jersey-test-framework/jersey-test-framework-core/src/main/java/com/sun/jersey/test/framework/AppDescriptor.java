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

package com.sun.jersey.test.framework;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * The base application descriptor.
 *
 * @author Paul.Sandoz@Sun.COM
 */
public abstract class AppDescriptor {
    
    /**
     * The base builder for building an application descriptor.
     * <p>
     * If properties of the builder are not modified default values be utilized.
     * The default value for client configuration is an instance of
     * {@link DefaultClientConfig}.
     * <p>
     * After the {@link #build() } has been invoked the state of the builder
     * will be reset to the default values.
     * @param <T> the type of the builder.
     * @param <V> the type of the descriptor
     */
    protected static abstract class AppDescriptorBuilder<T extends AppDescriptorBuilder, V extends AppDescriptor> {
        protected ClientConfig cc;

        /**
         * Set the client configuration.
         *
         * @param cc the client configuration.
         * @return this builder.
         */
        public T clientConfig(ClientConfig cc) {
            if (cc == null)
                throw new IllegalArgumentException("The client configuration must not be null");

            this.cc = cc;
            return (T)this;
        }

        public abstract V build();

        protected void reset() {
           this.cc = null;

        }
    }

    private final ClientConfig cc;

    protected AppDescriptor(AppDescriptorBuilder<?, ?> b) {
        this.cc = (b.cc == null)
                ? new DefaultClientConfig() : b.cc;
    }

    /**
     * Get the client configuration.
     *
     * @return the client configuration.
     */
    public ClientConfig getClientConfig() {
        return cc;
    }

}