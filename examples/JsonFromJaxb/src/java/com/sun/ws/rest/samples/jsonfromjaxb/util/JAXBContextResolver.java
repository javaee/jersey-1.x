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

package com.sun.ws.rest.samples.jsonfromjaxb.util;

import com.sun.ws.rest.impl.json.JSONJAXBContext;
import com.sun.ws.rest.samples.jsonfromjaxb.jaxb.FlightType;
import com.sun.ws.rest.samples.jsonfromjaxb.jaxb.Flights;
import com.sun.ws.rest.spi.service.ContextResolver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author japod
 */
@Provider
public final class JAXBContextResolver implements ContextResolver<JAXBContext> {
    
    private final JAXBContext context;
    
    private final Set<Class> types;
    
    private final Class[] cTypes = {Flights.class, FlightType.class};
    
    public JAXBContextResolver() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "BADGERFISH");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
        this.types = new HashSet(Arrays.asList(cTypes));
        this.context = new JSONJAXBContext(cTypes, props);
    }
    
    public JAXBContext getContext(Class<?> objectType) {
        return (types.contains(objectType)) ? context : null;
    }
}