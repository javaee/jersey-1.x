/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.ParamException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.model.method.dispatch.FormDispatchProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.ExtractorContainerException;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorProvider;
import com.sun.jersey.spi.inject.Injectable;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class FormParamInjectableProvider extends BaseParamInjectableProvider<FormParam> {

    private static final class FormParamInjectable extends AbstractHttpContextInjectable<Object> {

        private final MultivaluedParameterExtractor extractor;

        FormParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
        }

        @Override
        public Object getValue(HttpContext context) {

            Form form = getCachedForm(context);

            if (form == null) {
                form = getForm(context);
                cacheForm(context, form);
            }

            try {
                return extractor.extract(form);
            } catch (ExtractorContainerException e) {
                throw new ParamException.FormParamException(e.getCause(),
                        extractor.getName(), extractor.getDefaultStringValue());
            }
        }

        private void cacheForm(final HttpContext context, final Form form) {
            context.getProperties().put(FormDispatchProvider.FORM_PROPERTY, form);
        }

        private Form getCachedForm(final HttpContext context) {
            return (Form) context.getProperties().get(FormDispatchProvider.FORM_PROPERTY);
        }

        private HttpRequestContext ensureValidRequest(final HttpRequestContext r) throws IllegalStateException {
            if (r.getMethod().equals("GET")) {
                throw new IllegalStateException(
                        "The @FormParam is utilized when the request method is GET");
            }

            if (!MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, r.getMediaType())) {
                throw new IllegalStateException(
                        "The @FormParam is utilized when the content type of the request entity "
                        + "is not application/x-www-form-urlencoded");
            }
            return r;
        }

        private Form getForm(HttpContext context) {
            final HttpRequestContext r = ensureValidRequest(context.getRequest());
            return r.getFormParameters();
        }
    }

    public FormParamInjectableProvider(MultivaluedParameterExtractorProvider w) {
        super(w);
    }

    @Override
    public Injectable getInjectable(ComponentContext ic, FormParam a, Parameter c) {
        String parameterName = c.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid query parameter name
            return null;
        }

        MultivaluedParameterExtractor e = get(c);
        if (e == null) {
            return null;
        }

        return new FormParamInjectable(e, !c.isEncoded());
    }
}