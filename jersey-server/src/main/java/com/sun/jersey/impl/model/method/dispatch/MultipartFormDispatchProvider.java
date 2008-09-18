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

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.impl.http.header.reader.HttpHeaderReader;
import com.sun.jersey.impl.http.header.reader.HttpHeaderReaderImpl;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
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
        if (m != null && m.isCompatible(MULTIPART_FORM_DATA)) {
            MimeMultipart form = context.getRequest().getEntity(MimeMultipart.class);
            try {
                Map<String, BodyPart> formMap = getFormData(form);
                context.getProperties().put("com.sun.jersey.api.representation.form", formMap);
                context.getProperties().put("com.sun.jersey.api.representation.form.multipart", form);
            } catch(Exception e) {
                throw new ContainerException(e);
            }
        } else {
            Form form = context.getRequest().getEntity(Form.class);
            context.getProperties().put("com.sun.jersey.api.representation.form", form);
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
    
    private final class MultipartFormInjectable implements Injectable<Object> {
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
    
    private static final class MultipartFormParamInjectable implements Injectable<Object> {
        private final MessageBodyWorkers mbws;
        private final Parameter p;
        
        MultipartFormParamInjectable(MessageBodyWorkers mbws, Parameter p) {
            this.mbws = mbws;
            this.p = p;
        }
        
        @SuppressWarnings("unchecked")
        public Object getValue(HttpContext context) {
            try {
                Object o = context.getProperties().get("com.sun.jersey.api.representation.form");
                if (o instanceof Form) {
                    return getAsForm((Form)o, context);                    
                } else {
                    return getAsMultipartFormData((Map<String, BodyPart>)o, context);
                }                
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }                   
        }
        
        @SuppressWarnings("unchecked")
        private Object getAsForm(Form form, HttpContext context) throws Exception {
            String c = form.getFirst(p.getSourceName());
            InputStream is = new ByteArrayInputStream(c.getBytes("UTF-8"));
            MessageBodyReader r = mbws.getMessageBodyReader(
                    p.getParameterClass(), 
                    p.getParameterType(), 
                    p.getAnnotations(), 
                    MediaType.TEXT_PLAIN_TYPE);
            
            return r.readFrom(
                    p.getParameterClass(), 
                    p.getParameterType(), 
                    p.getAnnotations(), 
                    MediaType.TEXT_PLAIN_TYPE, 
                    context.getRequest().getRequestHeaders(), 
                    is);
        }
        
        @SuppressWarnings("unchecked")
        private Object getAsMultipartFormData(Map<String, BodyPart> formMap, HttpContext context) 
                throws Exception {
            BodyPart bp = formMap.get(p.getSourceName());
            if (bp == null)
                return null;

            MediaType m = (bp.getContentType() == null) 
                    ? MediaType.TEXT_PLAIN_TYPE 
                    : MediaType.valueOf(bp.getContentType());

            MessageBodyReader r = mbws.getMessageBodyReader(
                    p.getParameterClass(), 
                    p.getParameterType(), 
                    p.getAnnotations(), 
                    m);

            return r.readFrom(
                    p.getParameterClass(), 
                    p.getParameterType(), 
                    p.getAnnotations(), 
                    m, 
                    context.getRequest().getRequestHeaders(), 
                    bp.getInputStream());
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
                is.add(new MultipartFormParamInjectable(mbws, p));
            } else {
                Injectable injectable = ipc.getInjectable(p, Scope.PerRequest);
                is.add(injectable);
            }
        }
        return is;
    }    
    
    private static Map<String, BodyPart> getFormData(MimeMultipart mm) throws Exception {
        Map<String, BodyPart> m = new HashMap<String, BodyPart>();
        
        for (int i = 0; i < mm.getCount(); i++) {
            BodyPart b = mm.getBodyPart(i);
            if (b.getDisposition() != null && 
                    b.getDisposition().equalsIgnoreCase("form-data")) {
                String name = getName(b.getHeader("content-disposition")[0]);
                if (name != null)
                    m.put(name, b);
            }
        }
        return m;
    }
    
    private static String getName(String disposition) throws ParseException {
        HttpHeaderReader reader = new HttpHeaderReaderImpl(disposition);
        // Skip any white space
        reader.hasNext();

        // Get the "form-data"
        reader.nextToken();

        while (reader.hasNext()) {
            reader.nextSeparator(';');
            
            // Ignore a ';' with no parameters
            if (!reader.hasNext())
                break;
            
            // Get the parameter name
            String name = reader.nextToken();
            reader.nextSeparator('=');
            // Get the parameter value
            String value = reader.nextTokenOrQuotedString();
            if (name.equalsIgnoreCase("name")) {
                return value;
            }
        }
        return null;
    }
}