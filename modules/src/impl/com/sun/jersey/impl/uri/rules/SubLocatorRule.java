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

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestInjectable;
import com.sun.jersey.spi.inject.SingletonInjectable;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRuleContext;
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

    private final List<Injectable> is;
    
    private final Method m;

    public SubLocatorRule(UriTemplate template,
            Method m, List<Injectable> is) {
        super(template);
        this.m = m;
        this.is = is;
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
        context.pushResource(resource, getTemplate());
        
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
            if (is.size() == 0) {
                return m.invoke(resource);
            } else {
                final Object[] params = new Object[is.size()];
                int index = 0;
                for (Injectable i : is) {
                    params[index++] = i.getValue(context);                        
                }
                
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