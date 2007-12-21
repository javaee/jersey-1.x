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

import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.uri.PathPattern;
import java.util.List;
import java.util.Map;
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
            checkForJAXB();
        } catch(ClassNotFoundException e) {
            LOGGER.warning("WADL generation is disabled " +
                    "because JAXB jars are not " + 
                    "included in the java class path. " +
                    "To enable WADL include JAXB 2.x jars in the java class path."); 
            return null;
        }
        
        return new WadlResource(rootResources);
    }
    
    /**
     * Create the WADL resource method for GET.
     * <p>
     * This is created using reflection so that there is no runtime
     * dependency on JAXB. If the JAXB jars are not in the class path
     * then WADL generation will not be supported.
     * 
     * @param resource the resource model
     * @return the WADL resource method
     */
    public static ResourceMethod createWadlGetMethod(AbstractResource resource, PathPattern p) {
        try {
            checkForJAXB();
        } catch(ClassNotFoundException e) {
            return null;
        }
        
        if (p == null) {
            return new WadlMethodFactory.WadlGetMethod(resource, null);
        } else {
            // Remove the '/' from the beginning
            String path = p.getTemplate().getTemplate().substring(1);
            return new WadlMethodFactory.WadlGetMethod(resource, path);
        }        
    }    
    
    /**
     * Create the WADL resource method for OPTIONS.
     * <p>
     * This is created using reflection so that there is no runtime
     * dependency on JAXB. If the JAXB jars are not in the class path
     * then WADL generation will not be supported.
     * 
     * @param resource the resource model
     * @return the WADL resource OPTIONS method
     */
    public static ResourceMethod createWadlOptionsMethod(
            Map<String, List<ResourceMethod>> methods, 
            AbstractResource resource, PathPattern p) {
        try {
            checkForJAXB();
        } catch(ClassNotFoundException e) {
            return null;
        }
        
        if (p == null) {
            return new WadlMethodFactory.WadlOptionsMethod(methods, resource, null);
        } else {
            // Remove the '/' from the beginning
            String path = p.getTemplate().getTemplate().substring(1);
            return new WadlMethodFactory.WadlOptionsMethod(methods, resource, path);
        }
    }        
    
    /**
     * Check if JAXB is present in the class path
     * 
     * @throws java.lang.ClassNotFoundException
     */
    private static void checkForJAXB() throws ClassNotFoundException {
        Class<?> c = Class.forName("javax.xml.bind.JAXBElement");
    }
}
