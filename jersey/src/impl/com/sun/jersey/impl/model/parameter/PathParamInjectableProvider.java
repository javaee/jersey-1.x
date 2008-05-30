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

package com.sun.jersey.impl.model.parameter;

import com.sun.jersey.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.impl.model.parameter.multivalued.MultivaluedParameterProcessor;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class PathParamInjectableProvider implements 
        InjectableProvider<PathParam, Parameter> {
    
    private static final class PathParamInjectable implements Injectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;
        
        PathParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }
        
        public Object getValue(HttpContext context) {
            return extractor.extract(context.getUriInfo().getPathParameters(decode));
        }
    }
    
    private static final class PathParamSegmentInjectable implements Injectable<PathSegment> {
        private final String name;
        private final boolean decode;
        
        PathParamSegmentInjectable(String name, boolean decode) {
            this.name = name;
            this.decode = decode;
        }
        
        public PathSegment getValue(HttpContext context) {
            return context.getUriInfo().getPathSegment(name, decode);
        }
    }
    
    public Scope getScope() {
        return Scope.PerRequest;
    }
    
    public Injectable<?> getInjectable(InjectableContext ic, PathParam a, Parameter c) {
        String parameterName = c.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid URI parameter name
            return null;
        }
        
        if (c.getParameterClass() == PathSegment.class) {
            return new PathParamSegmentInjectable(parameterName, 
                    !c.isEncoded());
        } else {
            MultivaluedParameterExtractor e =  MultivaluedParameterProcessor.
                    process(c.getParameterClass(), 
                    c.getParameterType(), 
                    parameterName);        
            if (e == null)
                return null;

            return new PathParamInjectable(e, !c.isEncoded());
        }        
    }
}
