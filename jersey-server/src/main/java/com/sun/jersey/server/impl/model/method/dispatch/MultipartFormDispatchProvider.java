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

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterProcessor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MultipartFormDispatchProvider extends FormDispatchProvider {
    private static MediaType MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");
    
    @Override
    protected void processForm(HttpContext context) {
        MediaType m = context.getRequest().getMediaType();
        if (MediaTypes.typeEquals(MULTIPART_FORM_DATA, m)) {
            MimeMultipart form = context.getRequest().getEntity(MimeMultipart.class);
            try {
                Map<String, FormDataBodyPart> formMap = getFormData(form);
                context.getProperties().put("com.sun.jersey.api.representation.form", formMap);
                context.getProperties().put("com.sun.jersey.api.representation.form.multipart", form);
            } catch(Exception e) {
                throw new ContainerException(e);
            }
        } else {
            super.processForm(context);
        }
    }
    
    @Override
    public RequestDispatcher create(AbstractResourceMethod method) {
        boolean found = false;
        for (MediaType m : method.getSupportedInputTypes()) {
            found = (!m.isWildcardSubtype() && m.isCompatible(MULTIPART_FORM_DATA));
            if (found) break;
        }
        if (!found)
            return null;
        
        return super.create(method);
    }
        
    @Context MessageBodyWorkers mbws;
    
    private final class MultipartFormInjectable extends AbstractHttpContextInjectable<Object> {
        final Class<?> c;
        final Type t;
        final Annotation[] as;
        
        MultipartFormInjectable(Class c, Type t, Annotation[] as) {
            this.c = c;
            this.t = t;
            this.as = as;
        }

        public Object getValue(HttpContext context) {
            Object o = context.getProperties().get("com.sun.jersey.api.representation.form.multipart");
            if (o != null) return o;
            return context.getProperties().get("com.sun.jersey.api.representation.form");
        }        
    }
    
    private static final class DispositionParamInjectable
            extends AbstractHttpContextInjectable<FormDataContentDisposition> {

        private final String name;

        DispositionParamInjectable(Parameter p) {
            this.name = p.getSourceName();
        }

        @Override
        public FormDataContentDisposition getValue(HttpContext context) {
            try {
                Object o = context.getProperties().get("com.sun.jersey.api.representation.form");
                if (o instanceof Form) {
                    return null;
                } else {
                    Map<String, FormDataBodyPart> formMap = (Map<String, FormDataBodyPart>)o;
                    FormDataBodyPart fdbp = formMap.get(name);
                    if (fdbp == null)
                        return null;
                    else
                        return fdbp.fdcd;
                }
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }
        }
    }

    private static final class MultipartFormParamInjectable extends AbstractHttpContextInjectable<Object> {
        private final MessageBodyWorkers mbws;
        private final Parameter p;
        private final MultivaluedParameterExtractor extractor;

        MultipartFormParamInjectable(MessageBodyWorkers mbws, Parameter p) {
            this.mbws = mbws;
            this.p = p;
            this.extractor = MultivaluedParameterProcessor.
                    process(p.getDefaultValue(), p.getParameterClass(),
                    p.getParameterType(), p.getSourceName());
        }
        
        @SuppressWarnings("unchecked")
        public Object getValue(HttpContext context) {
            try {
                Object o = context.getProperties().get("com.sun.jersey.api.representation.form");
                if (o instanceof Form) {
                    return getAsForm((Form)o, context);                    
                } else {
                    return getAsMultipartFormData((Map<String, FormDataBodyPart>)o, context);
                }                
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }                   
        }
        
        @SuppressWarnings("unchecked")
        private Object getAsForm(Form form, HttpContext context) throws Exception {
            MessageBodyReader r = mbws.getMessageBodyReader(
                    p.getParameterClass(), 
                    p.getParameterType(), 
                    p.getAnnotations(), 
                    MediaType.TEXT_PLAIN_TYPE);
            
            if (r != null) {
                String c = form.getFirst(p.getSourceName());
                if (c == null)
                    return null;
                
                InputStream is = new ByteArrayInputStream(c.getBytes("UTF-8"));
                return r.readFrom(
                        p.getParameterClass(),
                        p.getParameterType(),
                        p.getAnnotations(),
                        MediaType.TEXT_PLAIN_TYPE,
                        context.getRequest().getRequestHeaders(),
                        is);
            } else if (extractor != null) {
                return extractor.extract(form);
            } else
                return null;
        }
        
        @SuppressWarnings("unchecked")
        private Object getAsMultipartFormData(Map<String, FormDataBodyPart> formMap, HttpContext context)
                throws Exception {
            FormDataBodyPart fdbp = formMap.get(p.getSourceName());
            if (fdbp == null)
                return null;

            MediaType m = (fdbp.bp.getContentType() == null)
                    ? MediaType.TEXT_PLAIN_TYPE 
                    : MediaType.valueOf(fdbp.bp.getContentType());

            MessageBodyReader r = mbws.getMessageBodyReader(
                    p.getParameterClass(), 
                    p.getParameterType(), 
                    p.getAnnotations(), 
                    m);

            if (r != null) {
                return r.readFrom(
                        p.getParameterClass(),
                        p.getParameterType(),
                        p.getAnnotations(),
                        m,
                        context.getRequest().getRequestHeaders(),
                        fdbp.bp.getInputStream());
            } else if (extractor != null) {
                r = mbws.getMessageBodyReader(
                    String.class,
                    String.class,
                    p.getAnnotations(),
                    m);
                
                String v = (String)r.readFrom(
                        String.class,
                        String.class,
                        p.getAnnotations(),
                        m,
                        context.getRequest().getRequestHeaders(),
                        fdbp.bp.getInputStream());
                MultivaluedMap<String, String> mvm = new MultivaluedMapImpl();
                mvm.putSingle(p.getSourceName(), v);
                return extractor.extract(mvm);
            } else {
                return null;
            }
        }
    }
    
    @Override
    protected List<Injectable> getInjectables(AbstractResourceMethod method) {
        List<Injectable> is = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter p = method.getParameters().get(i);
            
            if (Parameter.Source.ENTITY == p.getSource()) {
                if (MimeMultipart.class.isAssignableFrom(p.getParameterClass()) ||
                        MultivaluedMap.class.isAssignableFrom(p.getParameterClass())) {
                    is.add(new MultipartFormInjectable(p.getParameterClass(),
                            p.getParameterType(), p.getAnnotations()));
                } else {
                    is.add(null);                    
                }
            } else if (p.getAnnotation().annotationType() == FormParam.class) {
                if (FormDataContentDisposition.class == p.getParameterClass()) {
                    is.add(new DispositionParamInjectable(p));
                } else {
                    is.add(new MultipartFormParamInjectable(mbws, p));
                }
            } else {
                Injectable injectable = sipc.getInjectable(p, ComponentScope.PerRequest);
                is.add(injectable);
            }
        }
        return is;
    }    

    private static class FormDataBodyPart {
        final BodyPart bp;
        final FormDataContentDisposition fdcd;
        
        FormDataBodyPart(BodyPart bp, FormDataContentDisposition fdcd) {
            this.bp = bp;
            this.fdcd = fdcd;
        }
    }

    private static Map<String, FormDataBodyPart> getFormData(MimeMultipart mm) throws Exception {
        Map<String, FormDataBodyPart> m = new HashMap<String, FormDataBodyPart>();
        
        for (int i = 0; i < mm.getCount(); i++) {
            BodyPart b = mm.getBodyPart(i);
            if (b.getDisposition() != null &&
                    b.getDisposition().equalsIgnoreCase("form-data")) {
                FormDataContentDisposition fdcd = new FormDataContentDisposition(
                        b.getHeader("content-disposition")[0]);
                if (fdcd.getName() != null)
                    m.put(fdcd.getName(), new FormDataBodyPart(b, fdcd));
            }
        }
        return m;
    }
}