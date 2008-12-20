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

package com.sun.jersey.multipart.impl;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.impl.model.method.dispatch.FormDispatchProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterProcessor;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * <p>Support <code>@FormParam</code> injection into method parameters from
 * a {@link FormDataMultiPart} entity.</p>
 */
public class FormDataMultiPartDispatchProvider extends FormDispatchProvider {


    // ------------------------------------------------------------ Constructors


    public FormDataMultiPartDispatchProvider() {
        super();
    }


    // -------------------------------------------------------- Static Variables


    private static MediaType MULTIPART_FORM_DATA =
            new MediaType("multipart", "x-form-data");


    // ------------------------------------------------------ Instance Variables


    @Context
    MessageBodyWorkers mbws;


    @Context
    ServerInjectableProviderContext sipContext;


    // -------------------------------------------------------- Subclass Methods


    @Override
    public RequestDispatcher create(AbstractResourceMethod method) {
        boolean found = false;
        for (MediaType m : method.getSupportedInputTypes()) {
            found = (!m.isWildcardSubtype() && m.isCompatible(MULTIPART_FORM_DATA));
            if (found) {
                break;
            }
        }
        if (!found) {
            return null;
        }
        return super.create(method);
    }


    @Override
    protected List<Injectable> getInjectables(AbstractResourceMethod method) {
        List<Injectable> list = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter p = method.getParameters().get(i);
            if (Parameter.Source.ENTITY == p.getSource()) {
                if (FormDataMultiPart.class.isAssignableFrom(p.getParameterClass())
                    || MultivaluedMap.class.isAssignableFrom(p.getParameterClass())) {
                    list.add(new FormDataMultiPartInjectable(p.getParameterClass(),
                                                             p.getParameterType(),
                                                             p.getAnnotations()));
                } else {
                    list.add(null);
                }
            } else if (p.getAnnotation().annotationType() == FormParam.class) {
//                if (FormDataContentDisposition.class == p.getParameterClass()) {
//                    list.add(new DispositionParamInjectable(p));
//                } else {
                    list.add(new FormDataMultiPartParamInjectable(mbws, p));
//                }
            } else {
                Injectable injectable = sipContext.getInjectable(p, ComponentScope.PerRequest);
                list.add(injectable);
            }
        }
        return list;
    }


    @Override
    protected void processForm(HttpContext context) {
        MediaType m = context.getRequest().getMediaType();
        if ((m != null) && m.isCompatible(MULTIPART_FORM_DATA)) {
            FormDataMultiPart form = context.getRequest().getEntity(FormDataMultiPart.class);
            context.getProperties().put("com.sun.jersey.api.representation.form.multipart", form);
//        } else {
//            Form form = context.getRequest().getEntity(Form.class);
//            context.getProperties().put("com.sun.jersey.api.representation.form", form);
        }
    }


    // --------------------------------------------------------- Private Classes


    private final class FormDataMultiPartInjectable
            extends AbstractHttpContextInjectable<Object> {

        FormDataMultiPartInjectable(Class clazz, Type type, Annotation[] annotations) {
            this.clazz = clazz;
            this.type = type;
            this.annotations = annotations;
        }

        final Class<?> clazz;
        final Type type;
        final Annotation[] annotations;

        @Override
        public Object getValue(HttpContext context) {
            // Return entire FormDataMultiPart instance (if any)
            return context.getProperties().get("com.sun.jersey.api.representation.form.multipart");
        }

    }


    private final class FormDataMultiPartParamInjectable
            extends AbstractHttpContextInjectable<Object> {

        FormDataMultiPartParamInjectable(MessageBodyWorkers mbws, Parameter param) {
            this.mbws = mbws;
            this.param = param;
            this.extractor = MultivaluedParameterProcessor.
                    process(param.getDefaultValue(),
                            param.getParameterClass(),
                            param.getParameterType(),
                            param.getSourceName());
        }

        private final MessageBodyWorkers mbws;
        private final Parameter param;
        private final MultivaluedParameterExtractor extractor;

        @Override
        public Object getValue(HttpContext context) {
            // Return the field value for the field specified by the
            // sourceName property
            FormDataMultiPart fdmp = (FormDataMultiPart)
                    context.getProperties().get("com.sun.jersey.api.representation.form.multipart");
            if (fdmp == null) {
                return param.getDefaultValue();
            }
            FormDataBodyPart fdbp = fdmp.getField(param.getSourceName());
            if (fdbp == null) {
                return param.getDefaultValue();
            }
            MediaType mediaType = fdbp.getMediaType();
            if (mediaType == null) {
                mediaType = MediaType.TEXT_PLAIN_TYPE;
            }
            MessageBodyReader reader = mbws.getMessageBodyReader(
                    param.getParameterClass(),
                    param.getParameterType(),
                    param.getAnnotations(),
                    mediaType);
            if (reader != null) {
                try {
                    return reader.readFrom(
                            param.getParameterClass(),
                            param.getParameterType(),
                            param.getAnnotations(),
                            mediaType,
                            context.getRequest().getRequestHeaders(),
                            ((BodyPartEntity) fdbp.getEntity()).getInputStream());
                } catch (Exception e) {
                    throw new ContainerException(e);
                }
            } else if (extractor != null) {
                reader = mbws.getMessageBodyReader(
                        String.class,
                        String.class,
                        param.getAnnotations(),
                        mediaType);
                try {
                    String value = (String) reader.readFrom(
                            String.class,
                            String.class,
                            param.getAnnotations(),
                            mediaType,
                            context.getRequest().getRequestHeaders(),
                            ((BodyPartEntity) fdbp.getEntity()).getInputStream());
                    MultivaluedMap<String,String> map = new MultivaluedMapImpl();
                    map.putSingle(param.getSourceName(), value);
                    return extractor.extract(map);
                } catch (Exception e) {
                    throw new ContainerException(e);
                }
            } else {
                return null;
            }
        }
    }


}
