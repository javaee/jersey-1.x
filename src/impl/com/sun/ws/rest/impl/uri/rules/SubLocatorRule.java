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

package com.sun.ws.rest.impl.uri.rules;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRuleContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.WebApplicationException;

/**
 * The rule for accepting a sub-locator method.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class SubLocatorRule extends BaseRule {

    private final ParameterExtractor[] extractors;
    
    private final Method m;

    public SubLocatorRule(List<String> groupNames,
            Method m, ParameterExtractor[] extractors) {
        super(groupNames);
        this.m = m;
        this.extractors = extractors;
    }

    public boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        // Set the template values
        setTemplateValues(context);

        // Invoke the sub-locator to get the sub-resource
        resource = invokeSubLocator(resource, context);

        // Check if instance is a class
        if (resource instanceof Class) {
            // If so then get the instance of that class
            resource = context.getResource((Class)resource);
        }
        context.pushResource(resource);
        
        // Match sub-rules on the returned resource class
        final Iterator<UriRule> matches = context.getRules(resource.getClass()).
                match(path, context.getGroupValues());
        while(matches.hasNext())
            if(matches.next().accept(path, resource, context))
                return true;
        
        return false;            
    }
    
    private Object invokeSubLocator(Object resource, UriRuleContext context) {
        // Invoke the sub-locator method
        try {
            if (extractors == null) {
                return m.invoke(resource);
            } else {
                final Object[] params = new Object[extractors.length];
                for (int i = 0; i < extractors.length; i++)
                    params[i] = extractors[i].extract(context);
                
                return m.invoke(resource, params);
            }
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException)
                throw (RuntimeException)t;
            else
                throw new ContainerException(t);
        } catch (IllegalAccessException e) {
            throw new ContainerException(e);
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ContainerException("Exception injecting parameters to dynamic resolving method", e);
        }
    }
}