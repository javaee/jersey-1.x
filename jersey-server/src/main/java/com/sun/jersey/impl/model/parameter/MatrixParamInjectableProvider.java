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

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.impl.model.parameter.multivalued.MultivaluedParameterProcessor;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentContext;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.util.List;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MatrixParamInjectableProvider implements 
        InjectableProvider<MatrixParam, Parameter> {

    private static final class MatrixParamInjectable extends AbstractHttpContextInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;
        
        MatrixParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }
        
        public Object getValue(HttpContext context) {
            List<PathSegment> l = context.getUriInfo().getPathSegments(decode);
            PathSegment p = l.get(l.size() - 1);
            try {
                return extractor.extract(p.getMatrixParameters());
            } catch (ContainerException e) {
                throw new WebApplicationException(e.getCause(), 404);
            }
        }
    }

    public Scope getScope() {
        return Scope.PerRequest;
    }
    
    public Injectable getInjectable(ComponentContext ic, MatrixParam a, Parameter c) {
        String parameterName = c.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid header parameter name
            return null;
        }
        
        MultivaluedParameterExtractor e =  MultivaluedParameterProcessor.
                process(c.getDefaultValue(), c.getParameterClass(), 
                c.getParameterType(), parameterName);
        
        if (e == null)
            return null;
        
        return new MatrixParamInjectable(e, !c.isEncoded());
    }
}