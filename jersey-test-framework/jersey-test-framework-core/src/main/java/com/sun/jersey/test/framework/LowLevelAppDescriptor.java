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

package com.sun.jersey.test.framework;

import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
//import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;
//import com.sun.jersey.test.framework.spi.container.http.HTTPContainerFactory;
//import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A low-level application descriptor.
 * <p>
 * An instance of this class is created by creating an instance of
 * {@link Builder}, invoking methods to add/modify state, and finally invoking
 * the {@link Builder#build() } method.
 * <p>
 * This application descriptor is compatible with low-level test containers
 * that do not support Servlet. The following low-level test container
 * factories are provided:
 * <ul>
 *  <li>{@link GrizzlyTestContainerFactory} for testing with the low-level
 *      Grizzly HTTP container.</li>
 *  <li>{@link HTTPContainerFactory} for testing with the Light Weight HTTP
 *      server distributed with Java SE 6.</li>
 *  <li>{@link InMemoryTestContainerFactory} for testing in memory without
 *      using underlying HTTP client and server side functionality
 *      to send requests and receive responses.</li>
 * </ul>
 *
 * @author Paul.Sandoz@Sun.COM
 */
public class LowLevelAppDescriptor extends AppDescriptor {

    /**
     * The builder for building a low-level application descriptor.
     * <p>
     * If properties of the builder are not modified default values be utilized.
     * The default value for the context path is an empty string.
     * <p>
     * After the {@link #build() } has been invoked the state of the builder
     * will be reset to the default values.
     */
    public static class Builder
            extends AppDescriptorBuilder<Builder, LowLevelAppDescriptor> {

        protected final ResourceConfig rc;

        protected String contextPath = "";

        /**
         * Create a builder with one or more package names where
         * root resource and provider classes reside.
         * <p>
         * An instance of {@link PackagesResourceConfig} will be created and
         * set as the resource configuration.
         *
         * @param packages one or more package names where
         *        root resource and provider classes reside.
         * @throws IllegalArgumentException if <code>packages</code> is null.
         */
        public Builder(String... packages) throws IllegalArgumentException {
            if (packages == null)
                throw new IllegalArgumentException("The packages must not be null");
            
            this.rc = new PackagesResourceConfig(packages);
        }

        /**
         * Create a builder with one or more root resource and provider classes.
         * <p>
         * An instance of {@link ClassNamesResourceConfig} will be created and
         * set as the resource configuration.
         *
         * @param classes one or more root resource and provider classes.
         * @throws IllegalArgumentException if <code>classes</code> is null.
         */
        public Builder(Class... classes) throws IllegalArgumentException {
            if (classes == null)
                throw new IllegalArgumentException("The classes must not be null");

            this.rc = new ClassNamesResourceConfig(classes);
        }

        /**
         * Create a builder with a resource configuration.
         *
         * @param rc the resource configuration.
         * @throws IllegalArgumentException if <code>rc</code> is null.
         */
        public Builder(ResourceConfig rc) {
            if (rc == null)
                throw new IllegalArgumentException("The resource configuration must not be null");
            
            this.rc = rc;
        }

        /**
         * Set the context path.
         *
         * @param contextPath the context path to the application.
         * @return this builder.
         * @throws IllegalArgumentException if <code>contextPath</code> is null.
         */
        public Builder contextPath(String contextPath) {
            if (contextPath == null)
                throw new IllegalArgumentException("The context path must not be null");
            
            this.contextPath = contextPath;
            return this;
        }

        /**
         * Build the low-level application descriptor.
         * .
         * @return the low-level application descriptor.
         */
        public LowLevelAppDescriptor build() {
            LowLevelAppDescriptor lld = new LowLevelAppDescriptor(this);

            reset();
            
            return lld;
        }

        @Override
        protected void reset() {
            super.reset();

            this.contextPath = "";
        }
    }

    private final ResourceConfig rc;

    private final String contextPath;

    /**
     * The private constructor.
     * @param b {@link Builder} instance.
     */
    private LowLevelAppDescriptor(Builder b) {
        super(b);

        this.contextPath = b.contextPath;
        this.rc = b.rc;
    }

    /**
     * Get the resource configuration.
     *
     * @return the resource configuration.
     */
    public ResourceConfig getResourceConfig() {
        return rc;
    }

    /**
     * Get the context path.
     *
     * @return the context path.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Transform a Web-based application descriptor into a low-level
     * application descriptor.
     *
     * @param wad the Web-based application descriptor.
     * @return the low-level application descriptor.
     */
    public static LowLevelAppDescriptor transform(WebAppDescriptor wad) {
        // TODO need to check contraints on wad
        if (wad.getInitParams().get(PackagesResourceConfig.PROPERTY_PACKAGES) != null) {
            final Map<String, Object> init = new HashMap<String, Object>(wad.getInitParams());
            PackagesResourceConfig packagesResourceConfig = new PackagesResourceConfig(init);
            populateResourceConfigFeatures(packagesResourceConfig, wad.getInitParams());
            return new LowLevelAppDescriptor.Builder(packagesResourceConfig).build();
        } else if (wad.getInitParams().get(ClassNamesResourceConfig.PROPERTY_CLASSNAMES) != null) {
            String classes = wad.getInitParams().get(ClassNamesResourceConfig.PROPERTY_CLASSNAMES);
            ClassNamesResourceConfig classNamesResourceConfig = new ClassNamesResourceConfig(classes);
            populateResourceConfigFeatures(classNamesResourceConfig, wad.getInitParams());
            return new LowLevelAppDescriptor.Builder(classNamesResourceConfig).build();
        } else if (wad.getInitParams().get(ClasspathResourceConfig.PROPERTY_CLASSPATH) != null) {
            String classpath = wad.getInitParams().get(ClasspathResourceConfig.PROPERTY_CLASSPATH);
            ClasspathResourceConfig classpathResourceConfig = new ClasspathResourceConfig(classpath.split(";"));
            populateResourceConfigFeatures(classpathResourceConfig, wad.getInitParams());
            return new LowLevelAppDescriptor.Builder(classpathResourceConfig).build();
        }
        return null;
    }

    private static void populateResourceConfigFeatures(ResourceConfig rc, Map<String, String> initParams) {
        for(String initParam : initParams.keySet()) {
            if (initParams.get(initParam).equalsIgnoreCase("true") ||
                    initParams.get(initParam).equalsIgnoreCase("false")) {
                rc.getFeatures().put(initParam, new Boolean(initParams.get(initParam)));
            }
        }
    }
}
