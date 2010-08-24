/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.server.impl.model.parameter;

import com.sun.jersey.api.ParamException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.model.parameter.multivalued.ExtractorContainerException;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorProvider;
import com.sun.jersey.spi.inject.Injectable;
import javax.ws.rs.QueryParam;


/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class QueryParamInjectableProvider extends BaseParamInjectableProvider<QueryParam> {
    
    private static final class QueryParamInjectable extends AbstractHttpContextInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;
        
        QueryParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }
        
        public Object getValue(HttpContext context) {
            try {
                return extractor.extract(context.getUriInfo().getQueryParameters(decode));
            } catch (ExtractorContainerException e) {
                throw new ParamException.QueryParamException(e.getCause(),
                        extractor.getName(), extractor.getDefaultStringValue());
            }
        }
    }

    public QueryParamInjectableProvider(MultivaluedParameterExtractorProvider w) {
        super(w);
    }
    
    public Injectable getInjectable(ComponentContext ic, QueryParam a, Parameter c) {
        String parameterName = c.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid query parameter name
            return null;
        }
        
        MultivaluedParameterExtractor e = get(c);
        if (e == null)
            return null;
        
        return new QueryParamInjectable(e, !c.isEncoded());
    }
}