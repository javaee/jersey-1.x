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

package com.sun.ws.rest.impl.model.node;

import javax.ws.rs.WebApplicationException;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.spi.dispatch.ResourceDispatchContext;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class NodeDispatcher extends URITemplateDispatcher {
    final ParameterExtractor[] extractors;
    
    final Method m;

    int unmatchedPathExtractorIndex = -1;
    
    NodeDispatcher(final URITemplateType t, final Method m, ParameterExtractor[] extractors) {
        super(t);
        this.m = m;
        this.extractors = (extractors == null) ? new ParameterExtractor[0] : extractors;
        // TODO this is a bit of a hack but the extracting the
        // unmatched path requires access to a stack-based variable
        // so UnmatchedPathExtractor is a marker interface.
        for (int i = 0; i < this.extractors.length; i++) {
            if (this.extractors[i] instanceof UnmatchedPathExtractor) {
                this.unmatchedPathExtractorIndex = i;
                break;   
            }
        }
    }

    public boolean dispatch(ResourceDispatchContext context, Object node, String path) {
        try {
            if (extractors.length == 0) {
                node = m.invoke(node);
            } else {
                final Object[] params = new Object[extractors.length];
                for (int i = 0; i < extractors.length; i++) {
                    if (unmatchedPathExtractorIndex != i) {
                        params[i] = extractors[i].extract(context.getHttpRequestContext());
                    } else {
                        if (path != null && path.charAt(0) == '/')
                            path = path.substring(1);
                        params[i] = path;
                        // Consume the path
                        path = null;
                    }
                }
                
                node = m.invoke(node, params);
            }

            return context.dispatchTo(node, path);
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
