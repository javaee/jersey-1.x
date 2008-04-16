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
package com.sun.ws.rest.samples.jmaki.config;

import com.sun.ws.rest.impl.json.JSONJAXBContext;
import com.sun.ws.rest.samples.jmaki.beans.Printer;
import com.sun.ws.rest.samples.jmaki.beans.PrinterTableModel;
import com.sun.ws.rest.samples.jmaki.beans.TreeModel;
import com.sun.ws.rest.samples.jmaki.beans.WebResourceList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author japod
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private Class[] types = {Printer.class, PrinterTableModel.class, TreeModel.class, WebResourceList.class};

    public JAXBContextResolver() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED");
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);
        props.put(JSONJAXBContext.JSON_ARRAYS, "[\"rows\", \"cols\", \"children\", \"resources\"]");
        props.put(JSONJAXBContext.JSON_NON_STRINGS, "[\"expanded\"]");
        this.context = new JSONJAXBContext(types, props);
    }

    public JAXBContext getContext(Class<?> objectType) {
        for (Class type : types) {
            if (type == objectType) {
                return context;
            }
        }
        return null;
        
    }
}

