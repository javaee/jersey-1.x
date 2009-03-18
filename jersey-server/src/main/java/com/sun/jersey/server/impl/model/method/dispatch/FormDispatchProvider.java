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

package com.sun.jersey.server.impl.model.method.dispatch;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;


/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FormDispatchProvider extends AbstractResourceMethodDispatchProvider {
    public static final String FORM_PROPERTY = "com.sun.jersey.api.representation.form";
    
    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        if ("GET".equals(abstractResourceMethod.getHttpMethod())) {
            return null;
        }

        return super.create(abstractResourceMethod);
    }
            
    @Override
    protected InjectableValuesProvider getInjectableValuesProvider(AbstractResourceMethod abstractResourceMethod) {
        List<Injectable> is = processParameters(abstractResourceMethod);
        if (is == null)
            return null;

        return new FormParameterProvider(is);
    }

    @Context private MultivaluedParameterExtractorProvider mpep;

    protected MultivaluedParameterExtractorProvider getMultivaluedParameterExtractorProvider() {
        return mpep;
    }

    protected void processForm(HttpContext context) {
        Form form = (Form)context.getProperties().get(FORM_PROPERTY);
        if (form == null) {
            form = context.getRequest().getEntity(Form.class);
            context.getProperties().put(FORM_PROPERTY, form);
        }
    }

    private final class FormParameterProvider extends InjectableValuesProvider {
        public FormParameterProvider(List<Injectable> is) {
            super(is);
        }

        @Override
        public Object[] getInjectableValues(HttpContext context) {
            processForm(context);

            return super.getInjectableValues(context);
        }
    }

    private List<Injectable> processParameters(AbstractResourceMethod method) {        
        if (method.getParameters().isEmpty()) {
            return null;
        }
        
        boolean hasFormParam = false;
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter parameter = method.getParameters().get(i);
            if (parameter.getAnnotation() != null)
                hasFormParam |= parameter.getAnnotation().annotationType() == FormParam.class;            
        }
        if (!hasFormParam)
            return null;
        
        return getInjectables(method);
    }
    
    private static final class FormEntityInjectable extends AbstractHttpContextInjectable<Object> {
        final Class<?> c;
        final Type t;
        final Annotation[] as;

        FormEntityInjectable(Class c, Type t, Annotation[] as) {
            this.c = c;
            this.t = t;
            this.as = as;
        }

        public Object getValue(HttpContext context) {
            return context.getProperties().get(FORM_PROPERTY);
        }
    }

    private static final class FormParamInjectable extends AbstractHttpContextInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;

        FormParamInjectable(MultivaluedParameterExtractor extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }

        public Object getValue(HttpContext context) {
            Form form = (Form)
                    context.getProperties().get(FORM_PROPERTY);
            try {
                return extractor.extract(form);
            } catch (ContainerException e) {
                throw new WebApplicationException(e.getCause(), 400);
            }
        }
    }

    protected List<Injectable> getInjectables(AbstractResourceMethod method) {
        List<Injectable> is = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter p = method.getParameters().get(i);
            
            if (Parameter.Source.ENTITY == p.getSource()) {
                if (MultivaluedMap.class.isAssignableFrom(p.getParameterClass())) {
                    is.add(new FormEntityInjectable(p.getParameterClass(),
                            p.getParameterType(), p.getAnnotations()));
                } else
                    return null;
            } else if (p.getAnnotation().annotationType() == FormParam.class) {
                MultivaluedParameterExtractor e = mpep.get(p);
                if (e == null)
                    return null;
                is.add(new FormParamInjectable(e, !p.isEncoded()));
                
            } else {
                Injectable injectable = getInjectableProviderContext().
                        getInjectable(p, ComponentScope.PerRequest);
                if (injectable == null)
                    return null;
                is.add(injectable);
            }
        }
        return is;
    }
}