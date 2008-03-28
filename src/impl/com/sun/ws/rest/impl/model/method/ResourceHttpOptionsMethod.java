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

package com.sun.ws.rest.impl.model.method;

import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import com.sun.ws.rest.impl.model.MediaTypeHelper;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceHttpOptionsMethod extends ResourceMethod {
    
    public static class OptionsRequestDispatcher implements RequestDispatcher {
        protected final String allow;
        
        public OptionsRequestDispatcher(Map<String, List<ResourceMethod>> methods) {
            this.allow = getAllow(methods);
        }
        
        private String getAllow(Map<String, List<ResourceMethod>> methods) {
            StringBuilder s = new StringBuilder();
            boolean first = true;
            for (String method : methods.keySet()) {
                if (!first) s.append(",");
                first = false;

                s.append(method);
            }

            return s.toString();
        }
    
        public void dispatch(Object resource, HttpContext context) {
            Response r = Response.noContent().header("Allow", allow).build();
            context.getResponse().setResponse(r, null);
        }
    }
    
    public ResourceHttpOptionsMethod(Map<String, List<ResourceMethod>> methods) {
        super("OPTIONS",
                null,
                MediaTypeHelper.GENERAL_MEDIA_TYPE_LIST, 
                MediaTypeHelper.GENERAL_MEDIA_TYPE_LIST,
                new OptionsRequestDispatcher(methods));
    }
    
    @Override
    public String toString() {
        return "OPTIONS";
    }
}