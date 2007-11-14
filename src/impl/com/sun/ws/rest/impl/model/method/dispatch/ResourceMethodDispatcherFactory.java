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

package com.sun.ws.rest.impl.model.method.dispatch;

import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import com.sun.ws.rest.spi.service.ServiceFinder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceMethodDispatcherFactory {
    private static final Logger LOGGER = Logger.getLogger(ResourceMethodDispatcherFactory.class.getName());
    
    public static RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        for (ResourceMethodDispatchProvider ip : ServiceFinder.find(ResourceMethodDispatchProvider.class)) {
            try {
                RequestDispatcher d = ip.create(abstractResourceMethod);
                if (d != null)
                    return d;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                
                sw.write(ImplMessages.ERROR_PROCESSING_METHOD(abstractResourceMethod.getMethod(), ip.getClass().getName()));
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                
                LOGGER.severe(sw.toString());
            }
        }
        
        return null;        
    }
}