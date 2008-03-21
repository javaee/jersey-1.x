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

package com.sun.ws.rest.impl.provider.entity;

import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractJAXBElementProvider extends AbstractTypeEntityProvider<Object> {    
    private static Map<Class, JAXBContext> jaxbContexts = 
            new WeakHashMap<Class, JAXBContext>();

    @Context private ContextResolver<JAXBContext> cr;
    
    public final boolean supports(Class<?> type) {
        return type.getAnnotation(XmlRootElement.class) != null;
    }
    
    protected final JAXBContext getJAXBContext(Class type) throws JAXBException {
        if (cr != null) {
            JAXBContext c = cr.getContext(type);
            if (c != null) return c;
        }
        
        synchronized (jaxbContexts) {
            JAXBContext context = jaxbContexts.get(type);
            if (context == null) {
                context = JAXBContext.newInstance(type);
                jaxbContexts.put(type, context);
            }
            return context;
        }
    }
}