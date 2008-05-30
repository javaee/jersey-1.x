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

package com.sun.jersey.impl.model.method.dispatch;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpReqResDispatchProvider implements ResourceMethodDispatchProvider {
    
    static final class HttpReqResDispatcher extends ResourceJavaMethodDispatcher {
        HttpReqResDispatcher(AbstractResourceMethod abstractResourceMethod) {
            super(abstractResourceMethod);
        }

        public void _dispatch(Object resource, HttpContext context) 
        throws IllegalAccessException, InvocationTargetException {
            method.invoke(resource, context.getRequest(), context.getResponse());
            MediaType m = getAcceptableMediaType(context.getRequest());
            if (m != null) 
                context.getResponse().
                        getHttpHeaders().putSingle("Content-Type", m);
        }
    }

    
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        // TODO: add return type to ARM
        if (abstractResourceMethod.getMethod().getReturnType() != void.class) return null;
        
        // TODO: use ARM getParams instead
        Class<?>[] parameters = abstractResourceMethod.getMethod().getParameterTypes();
        if (parameters.length != 2) return null;
        if (parameters[0] != HttpRequestContext.class) return null;
        if (parameters[1] != HttpResponseContext.class) return null;
                
        return new HttpReqResDispatcher(abstractResourceMethod);
    }
}