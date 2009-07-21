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

package com.sun.jersey.server.impl.uri.rules;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.probes.UriRuleProbeProvider;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.inject.Injectable;
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

    private final List<AbstractHttpContextInjectable> is;
    
    private final Method m;

    private final List<ContainerRequestFilter> requestFilters;

    private final List<ContainerResponseFilter> responseFilters;
    
    public SubLocatorRule(UriTemplate template,
            Method m, 
            List<Injectable> is,
            List<ContainerRequestFilter> requestFilters,
            List<ContainerResponseFilter> responseFilters) {
        super(template);
        this.m = m;
        this.is = AbstractHttpContextInjectable.transform(is);
        this.requestFilters = requestFilters;
        this.responseFilters = responseFilters;
    }

    public boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        UriRuleProbeProvider.ruleAccept(SubLocatorRule.class.getSimpleName(), path,
                resource);

        // Set the template values
        pushMatch(context);

        // Invoke the sub-locator to get the sub-resource
        resource = invokeSubLocator(resource, context);
        // If null then no match
        if (resource == null)
            return false;
        
        // Check if instance is a class
        if (resource instanceof Class) {
            // If so then get the instance of that class
            resource = context.getResource((Class)resource);
        }
        context.pushResource(resource);
        
        // Match sub-rules on the returned resource class
        final Iterator<UriRule> matches = context.getRules(resource.getClass()).
                match(path, context);
        while(matches.hasNext())
            if(matches.next().accept(path, resource, context))
                return true;

        return false;            
    }
    
    private Object invokeSubLocator(Object resource, UriRuleContext context) {
        // Push the response filters
        context.pushContainerResponseFilters(responseFilters);
        
        // Process the request filter
        if (!requestFilters.isEmpty()) {
            ContainerRequest containerRequest = context.getContainerRequest();
            for (ContainerRequestFilter f : requestFilters) {
                containerRequest = f.filter(containerRequest);
                context.setContainerRequest(containerRequest);
            }
        }

        // Invoke the sub-locator method
        try {
            if (is.size() == 0) {
                return m.invoke(resource);
            } else {
                final Object[] params = new Object[is.size()];
                int index = 0;
                for (AbstractHttpContextInjectable i : is) {
                    params[index++] = i.getValue(context);                        
                }
                
                return m.invoke(resource, params);
            }
        } catch (InvocationTargetException e) {
            // Propagate the target exception so it may be mapped to a response
            throw new MappableContainerException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new ContainerException(e);
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ContainerException("Exception injecting parameters for sub-locator method: " + m, e);
        }
    }
}
