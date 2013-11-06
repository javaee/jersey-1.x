/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.api.wadl.config;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.server.wadl.WadlGenerator;
import java.security.AccessController;
import java.util.Collections;
import java.util.List;

/**
 * Loads a {@link WadlGeneratorConfig} and provides access to the {@link WadlGenerator}
 * provided by the loaded {@link WadlGeneratorConfig}.<br/>
 * If no {@link WadlGeneratorConfig} is provided, the default {@link WadlGenerator}
 * will be loaded.<br />
 *
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorConfigLoader {

    /**
     * Load the {@link WadlGeneratorConfig} from the provided {@link ResourceConfig} using the
     * property {@link ResourceConfig#PROPERTY_WADL_GENERATOR_CONFIG}.
     *
     * <p>
     * The type of this property must be a subclass or an instance of a subclass of
     * {@link WadlGeneratorConfig}.<br/>
     * If it's not set, the default {@link com.sun.jersey.server.wadl.generators.WadlGeneratorJAXBGrammarGenerator} will be used.
     * </p>
     *
     * @param resourceConfig
     * @return a configure {@link WadlGeneratorConfig}.
     */
    public static WadlGeneratorConfig loadWadlGeneratorsFromConfig( ResourceConfig resourceConfig ) {
        final Object wadlGeneratorConfigProperty = resourceConfig.getProperty(
                ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG );
        if ( wadlGeneratorConfigProperty == null ) {
            WadlGeneratorConfig config = new WadlGeneratorConfig() {
                        @Override
                        public List<WadlGeneratorDescription> configure() {
                            return Collections.EMPTY_LIST;
                        }
                    };
            return config;
        }
        else {

            try {

                if ( wadlGeneratorConfigProperty instanceof WadlGeneratorConfig ) {
                    return ( (WadlGeneratorConfig)wadlGeneratorConfigProperty );
                }

                final Class<? extends WadlGeneratorConfig> configClazz;
                if ( wadlGeneratorConfigProperty instanceof Class ) {
                    configClazz = ( (Class<?>)wadlGeneratorConfigProperty ).
                            asSubclass( WadlGeneratorConfig.class );
                }
                else if ( wadlGeneratorConfigProperty instanceof String ) {
                    configClazz = AccessController.doPrivileged(ReflectionHelper.classForNameWithExceptionPEA((String)wadlGeneratorConfigProperty)).
                            asSubclass( WadlGeneratorConfig.class );
                }
                else {
                    throw new RuntimeException( "The property " + ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG +
                            " is an invalid type: " + wadlGeneratorConfigProperty.getClass().getName() +
                            " (supported: String, Class<? extends WadlGeneratorConfiguration>," +
                            " WadlGeneratorConfiguration)" );
                }

                final WadlGeneratorConfig config = configClazz.newInstance();

                return config;

            } catch ( Exception e ) {
                throw new RuntimeException( "Could not load WadlGeneratorConfiguration," +
                        " check the configuration of " + ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, e );
            }
        }
    }

}
