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

package com.sun.ws.rest.impl.wadl;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.uri.PathPattern;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class WadlFactory {
    private static final Logger LOGGER = Logger.getLogger(WadlFactory.class.getName());
    
    /**
     * Create the WADL resource object.
     * <p>
     * This is created using reflection so that there is no runtime
     * dependency on JAXB. If the JAXB jars are not in the class path
     * then WADL generation will not be supported.
     * 
     * @param rootResources the set of root resources
     * @return the WADL resource object
     */
    public static Object createWadlResource(Set<AbstractResource> rootResources) {
        try {
            Class<?> wc = Class.forName("com.sun.ws.rest.impl.wadl.WadlResource");
            Constructor<?> wcc = wc.getConstructor(Set.class);
            return wcc.newInstance(rootResources);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof NoClassDefFoundError) {
                LOGGER.warning("WADL generation is disabled " +
                        "because dependent Java classes cannot be found. " + 
                        "This is most likely because JAXB jars are not " + 
                        "included in the java class path. " +
                        "To enable WADL include JAXB 2.x jars in the java class path.");
            } else {
                StringWriter s = new StringWriter();
                e.getCause().printStackTrace(new PrintWriter(s));
                LOGGER.severe("Error constructing WADL for root resources: " + s.toString());
            }
        } catch(RuntimeException e) {
            LOGGER.severe("Error configuring WADL support");
            throw e;
        } catch(Exception e) {
            LOGGER.severe("Error configuring WADL support");
            throw new ContainerException(e);
        }
        
        return null;
    }
    
    /**
     * Create the WADL resource method.
     * <p>
     * This is created using reflection so that there is no runtime
     * dependency on JAXB. If the JAXB jars are not in the class path
     * then WADL generation will not be supported.
     * 
     * @param resource the resource model
     * @return the WADL resource method
     */
    public static ResourceMethod createWadlMethod(AbstractResource resource, PathPattern p) {
        try {
            Class<?> wm = Class.forName("com.sun.ws.rest.impl.wadl.WadlMethod");
            if (p == null) {
                Constructor<?> wcc = wm.getConstructor(AbstractResource.class);
                return (ResourceMethod)wcc.newInstance(resource);
            } else {
                Constructor<?> wcc = wm.getConstructor(AbstractResource.class, 
                        String.class);
                // Remove the '/' from the beginning
                String path = p.getTemplate().getTemplate().substring(1);
                return (ResourceMethod)wcc.newInstance(resource, path);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof NoClassDefFoundError) {
                // Ignore
                // The warning will be printed out once when attempting to 
                // create the WADL of the application
            } else {
                StringWriter s = new StringWriter();
                e.getCause().printStackTrace(new PrintWriter(s));
                LOGGER.severe("Error constructing WADL for resource " + 
                        resource.getClass() + ": " + s.toString());
            }
        } catch(RuntimeException e) {
            LOGGER.severe("Error configuring WADL support");
            throw e;
        } catch(Exception e) {
            LOGGER.severe("Error configuring WADL support");
            throw new ContainerException(e);
        }
        
        return null;
    }    
}
