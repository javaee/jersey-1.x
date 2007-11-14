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

package com.sun.ws.rest.impl.dispatch;

import com.sun.ws.rest.spi.dispatch.UriPathResolver;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A URI path resolver implementation that sorts URI templates in order 
 * and linearly iterates through the ordered list to find a matching template.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class LinearOrderedUriPathResolver<T> implements UriPathResolver<T> {
    
    private static final Logger LOGGER = Logger.getLogger(LinearOrderedUriPathResolver.class.getName());
    
    Map<UriTemplateType, T> map = 
            new TreeMap<UriTemplateType, T>(UriTemplateType.COMPARATOR);
            
    public boolean add(UriTemplateType template, T t) {
        map.put(template, t);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Adding dispatcher: " + template + ":" + t.toString());
        }
        return true;
    }
    
    public Map<UriTemplateType, T> getUriTemplates() {
        return map;
    }
    
    public T resolve(CharSequence path, StringBuilder rightHandPath, 
            List<String> templateValues) {
        throw new UnsupportedOperationException();
    }
    
    public T resolve(CharSequence path, StringBuilder rightHandPath, 
            Map<String, String> templateValues) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("resolving: path="+path+",rightHandPath="+rightHandPath.toString());
        }
        // Iterate through the entry set
        for (Map.Entry<UriTemplateType, T> e : map.entrySet()) {
            // Match each template
            if (e.getKey().match(path, rightHandPath, templateValues)) {
                // Return the first match
                return e.getValue();
            }
        }    
        
        return null;
    }
}