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
package com.sun.jersey.impl.wadl.config;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.jersey.impl.wadl.WadlGeneratorImpl;

/**
 * Loads a {@link WadlGeneratorConfiguration} and provides access to the {@link WadlGenerator}
 * provides by the loaded {@link WadlGeneratorConfiguration}.<br/>
 * If no {@link WadlGeneratorConfiguration} is provided, the default {@link WadlGenerator}
 * will be loaded.<br />
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorConfigurationLoader {

    private static final Logger LOGGER = Logger.getLogger( WadlGeneratorConfigurationLoader.class.getName() );

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
                
                if ( wadlGeneratorConfigProperty instanceof WadlGeneratorConfiguration ) {
                    return ( (WadlGeneratorConfiguration)wadlGeneratorConfigProperty ).getWadlGenerator();
                }

                final Class<? extends WadlGeneratorConfiguration> configClazz;
                if ( wadlGeneratorConfigProperty instanceof Class ) {
                    configClazz = ( (Class<?>)wadlGeneratorConfigProperty ).asSubclass( WadlGeneratorConfiguration.class );
                }
                else if ( wadlGeneratorConfigProperty instanceof String ) {
                    configClazz = Class.forName( (String) wadlGeneratorConfigProperty ).asSubclass( WadlGeneratorConfiguration.class );
                }
                else {
                    throw new RuntimeException( "The property " + ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG + 
                            " is an invalid type: " + wadlGeneratorConfigProperty.getClass().getName() +
                            " (supported: String, Class<? extends WadlGeneratorConfiguration>," +
                            " WadlGeneratorConfiguration)" );
                }
                
                final Constructor<? extends WadlGeneratorConfiguration> mapConstructor = configClazz.getConstructor( Map.class );
                
                final WadlGeneratorConfiguration config;
                if ( mapConstructor != null ) {
                    config = mapConstructor.newInstance( resourceConfig.getProperties() );
                }
                else  {
                    config = configClazz.newInstance();
                }
                
                return config.getWadlGenerator();
                
            } catch ( Exception e ) {
                throw new RuntimeException( "Could not load WadlGeneratorConfiguration," +
                        " check the configuration of " + ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, e );
            }
        }
    }

}
