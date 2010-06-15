/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.multipart.impl;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorProvider;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * <p>Support <code>@FormDataParam</code> injection into method parameters from
 * a {@link FormDataMultiPart} entity.</p>
 */
public class FormDataMultiPartDispatchProvider extends AbstractResourceMethodDispatchProvider {
    private static final String FORM_MULTIPART_PROPERTY = "com.sun.jersey.api.representation.form.multipart";

    @Context
    private MessageBodyWorkers mbws;

    @Context
    private MultivaluedParameterExtractorProvider mpep;


    @Override
    public RequestDispatcher create(AbstractResourceMethod method) {
        if ("GET".equals(method.getHttpMethod())) {
            return null;
        }
        
        boolean found = false;
        for (MediaType m : method.getSupportedInputTypes()) {
            found = (!m.isWildcardSubtype() && m.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE));
            if (found) {
                break;
            }
        }
        if (!found) {
            return null;
        }
        
        return super.create(method);
    }

    private static final class FormDataInjectableValuesProvider extends InjectableValuesProvider {
        public FormDataInjectableValuesProvider(List<Injectable> is) {
            super(is);
        }

        @Override
        public Object[] getInjectableValues(HttpContext context) {
            FormDataMultiPart form = context.getRequest().getEntity(FormDataMultiPart.class);
            context.getProperties().put(FORM_MULTIPART_PROPERTY, form);

            return super.getInjectableValues(context);
        }
    }

    @Override
    protected InjectableValuesProvider getInjectableValuesProvider(AbstractResourceMethod method) {
        if (method.getParameters().isEmpty()) {
            return null;
        }

        boolean hasFormParam = false;
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter parameter = method.getParameters().get(i);
            if (parameter.getAnnotation() != null)
                hasFormParam |= parameter.getAnnotation().annotationType() == FormDataParam.class;
        }
        if (!hasFormParam)
            return null;

        return new FormDataInjectableValuesProvider(getInjectables(method));
    }

    private List<Injectable> getInjectables(AbstractResourceMethod method) {
        List<Injectable> list = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter p = method.getParameters().get(i);
            if (Parameter.Source.ENTITY == p.getSource()) {
                if (FormDataMultiPart.class.isAssignableFrom(p.getParameterClass())) {
                    list.add(new FormDataMultiPartInjectable());
                } else {
                    list.add(null);
                }
            } else if (p.getAnnotation().annotationType() == FormDataParam.class) {
                if (Collection.class == p.getParameterClass() || List.class == p.getParameterClass()) {
                    Class c = ReflectionHelper.getGenericClass(p.getParameterType());
                    if (FormDataBodyPart.class == c) {
                        list.add(new ListFormDataBodyPartMultiPartInjectable(p.getSourceName()));
                    } else if (FormDataContentDisposition.class == c) {
                        list.add(new ListFormDataContentDispositionMultiPartInjectable(p.getSourceName()));
                    }
                } else if (FormDataBodyPart.class == p.getParameterClass()) {
                    list.add(new FormDataBodyPartMultiPartInjectable(p.getSourceName()));
                } else if (FormDataContentDisposition.class == p.getParameterClass()) {
                    list.add(new FormDataContentDispositionMultiPartInjectable(p.getSourceName()));
                } else {
                    list.add(new FormDataMultiPartParamInjectable(p));
                }
            } else {
                Injectable injectable = getInjectableProviderContext().getInjectable(p, ComponentScope.PerRequest);
                list.add(injectable);
            }
        }
        return list;
    }

    private static final class FormDataMultiPartInjectable
            extends AbstractHttpContextInjectable<Object> {

        @Override
        public Object getValue(HttpContext context) {
            // Return entire FormDataMultiPart instance (if any)
            return context.getProperties().get(FORM_MULTIPART_PROPERTY);
        }

    }

    private static final class FormDataBodyPartMultiPartInjectable
            extends AbstractHttpContextInjectable<FormDataBodyPart> {
        private final String name;

        FormDataBodyPartMultiPartInjectable(String name) {
            this.name = name;
        }

        @Override
        public FormDataBodyPart getValue(HttpContext context) {
            FormDataMultiPart fdmp = (FormDataMultiPart)
                    context.getProperties().get(FORM_MULTIPART_PROPERTY);

            return fdmp.getField(name);
        }
    }

    private static final class ListFormDataBodyPartMultiPartInjectable
            extends AbstractHttpContextInjectable<List<FormDataBodyPart>> {
        private final String name;

        ListFormDataBodyPartMultiPartInjectable(String name) {
            this.name = name;
        }

        @Override
        public List<FormDataBodyPart> getValue(HttpContext context) {
            FormDataMultiPart fdmp = (FormDataMultiPart)
                    context.getProperties().get(FORM_MULTIPART_PROPERTY);

            return fdmp.getFields(name);
        }
    }

    private static final class FormDataContentDispositionMultiPartInjectable
            extends AbstractHttpContextInjectable<FormDataContentDisposition> {
        private final String name;

        FormDataContentDispositionMultiPartInjectable(String name) {
            this.name = name;
        }

        @Override
        public FormDataContentDisposition getValue(HttpContext context) {
            FormDataMultiPart fdmp = (FormDataMultiPart)
                    context.getProperties().get(FORM_MULTIPART_PROPERTY);

            FormDataBodyPart fdbp = fdmp.getField(name);
            if (fdbp == null)
                return null;
            
            return fdmp.getField(name).getFormDataContentDisposition();
        }
    }

    private static final class ListFormDataContentDispositionMultiPartInjectable
            extends AbstractHttpContextInjectable<List<FormDataContentDisposition>> {
        private final String name;

        ListFormDataContentDispositionMultiPartInjectable(String name) {
            this.name = name;
        }

        @Override
        public List<FormDataContentDisposition> getValue(HttpContext context) {
            FormDataMultiPart fdmp = (FormDataMultiPart)
                    context.getProperties().get(FORM_MULTIPART_PROPERTY);

            List<FormDataBodyPart> fdbps = fdmp.getFields(name);
            if (fdbps == null)
                return null;

            List<FormDataContentDisposition> l = new ArrayList<FormDataContentDisposition>(fdbps.size());
            for (FormDataBodyPart fdbp : fdbps) {
                l.add(fdbp.getFormDataContentDisposition());
            }

            return l;
        }
    }

    private final class FormDataMultiPartParamInjectable
            extends AbstractHttpContextInjectable<Object> {

        private final Parameter param;

        private final MultivaluedParameterExtractor extractor;

        FormDataMultiPartParamInjectable(Parameter param) {
            this.param = param;
            this.extractor = mpep.get(param);
        }

        @Override
        public Object getValue(HttpContext context) {
            // Return the field value for the field specified by the
            // sourceName property
            FormDataMultiPart fdmp = (FormDataMultiPart)
                    context.getProperties().get(FORM_MULTIPART_PROPERTY);
            
            List<FormDataBodyPart> fdbps = fdmp.getFields(param.getSourceName());
            FormDataBodyPart fdbp = (fdbps != null) ? fdbps.get(0) : null;

            MediaType mediaType = (fdbp != null)
                    ? fdbp.getMediaType() : MediaType.TEXT_PLAIN_TYPE;

            MessageBodyReader reader = mbws.getMessageBodyReader(
                    param.getParameterClass(),
                    param.getParameterType(),
                    param.getAnnotations(),
                    mediaType);
           
            if (reader != null) {
                InputStream in = null;
                if (fdbp == null) {
                    if (param.getDefaultValue() != null) {
                        // Convert default value to bytes
                        in = new ByteArrayInputStream(param.getDefaultValue().getBytes());
                    } else {
                        return null;
                    }
                } else {
                    in = ((BodyPartEntity) fdbp.getEntity()).getInputStream();
                }

                try {
                    return reader.readFrom(
                            param.getParameterClass(),
                            param.getParameterType(),
                            param.getAnnotations(),
                            mediaType,
                            context.getRequest().getRequestHeaders(),
                            in);
                } catch (IOException e) {
                    throw new ContainerException(e);
                }
            } else if (extractor != null) {
                MultivaluedMap<String, String> map = new MultivaluedMapImpl();
                if (fdbp != null) {
                    try {
                        for (FormDataBodyPart p : fdbps) {
                            mediaType = p.getMediaType();

                            reader = mbws.getMessageBodyReader(
                                    String.class,
                                    String.class,
                                    param.getAnnotations(),
                                    mediaType);

                            String value = (String) reader.readFrom(
                                    String.class,
                                    String.class,
                                    param.getAnnotations(),
                                    mediaType,
                                    context.getRequest().getRequestHeaders(),
                                    ((BodyPartEntity) p.getEntity()).getInputStream());

                            map.add(param.getSourceName(), value);
                        }
                    } catch (IOException e) {
                        throw new ContainerException(e);
                    }
                }
                return extractor.extract(map);
            } else {
                return null;
            }
        }
    }
}