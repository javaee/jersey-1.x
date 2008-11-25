/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.api.wadl.config;

import java.util.logging.Logger;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;

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

    private static final Logger LOGGER = Logger.getLogger( WadlGeneratorConfigLoader.class.getName() );

    public static WadlGenerator loadWadlGeneratorsFromConfig( ResourceConfig resourceConfig ) {
        final Object wadlGeneratorConfigProperty = resourceConfig.getProperty(
                ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG );
        if ( wadlGeneratorConfigProperty == null ) {
            final WadlGenerator wadlGenerator = new WadlGeneratorImpl();
            try {
                wadlGenerator.init();
                return wadlGenerator;
            } catch ( Exception e ) {
                throw new RuntimeException( "Could not init the " + wadlGenerator.getClass().getName(), e );
            }
        }
        else {

            try {
                
                if ( wadlGeneratorConfigProperty instanceof WadlGeneratorConfig ) {
                    return ( (WadlGeneratorConfig)wadlGeneratorConfigProperty ).getWadlGenerator();
                }

                final Class<? extends WadlGeneratorConfig> configClazz;
                if ( wadlGeneratorConfigProperty instanceof Class ) {
                    configClazz = ( (Class<?>)wadlGeneratorConfigProperty ).
                            asSubclass( WadlGeneratorConfig.class );
                }
                else if ( wadlGeneratorConfigProperty instanceof String ) {
                    configClazz = ReflectionHelper.classForName( (String) wadlGeneratorConfigProperty ).
                            asSubclass( WadlGeneratorConfig.class );
                }
                else {
                    throw new RuntimeException( "The property " + ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG + 
                            " is an invalid type: " + wadlGeneratorConfigProperty.getClass().getName() +
                            " (supported: String, Class<? extends WadlGeneratorConfiguration>," +
                            " WadlGeneratorConfiguration)" );
                }
                
                final WadlGeneratorConfig config = configClazz.newInstance();
                
                return config.getWadlGenerator();
                
            } catch ( Exception e ) {
                throw new RuntimeException( "Could not load WadlGeneratorConfiguration," +
                        " check the configuration of " + ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, e );
            }
        }
    }

}
